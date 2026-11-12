package be.nabu.eai.developer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.api.ConnectionTarget;
import be.nabu.eai.developer.base.ConnectionTunnelBase;

/** Runs a foreground command that binds the explicitly configured local endpoint. */
public class CLIConnectionTunnel extends ConnectionTunnelBase {
	private static final Logger logger = LoggerFactory.getLogger(CLIConnectionTunnel.class);
	private final String command;
	private Process process;
	private Thread shutdownHook;
	private final StringBuilder output = new StringBuilder();

	public CLIConnectionTunnel(String command, String host, int port) {
		this.command = command;
		setLocalHost(host);
		setLocalPort(port);
	}

	/** Whitespace separates arguments; single/double quotes group text, backslash escapes the next character. No shell expansion occurs. */
	public static List<String> parse(String command) {
		if (command == null) {
			throw new IllegalArgumentException("CLI command is required");
		}
		List<String> result = new ArrayList<String>();
		StringBuilder word = new StringBuilder();
		char quote = 0;
		boolean escaped = false, started = false;
		for (int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			if (escaped) {
				word.append(c);
				escaped = false;
			}
			else if (c == '\\' && quote != '\'' && i + 1 < command.length() && (Character.isWhitespace(command.charAt(i + 1)) || command.charAt(i + 1) == '"' || command.charAt(i + 1) == '\\' || command.charAt(i + 1) == '\'')) {
				escaped = true;
				started = true;
			}
			else if (quote != 0) {
				if (c == quote) {
					quote = 0;
				}
				else {
					word.append(c);
				}
			}
			else if (c == '\'' || c == '"') {
				quote = c;
				started = true;
			}
			else if (Character.isWhitespace(c)) {
				if (started) {
					result.add(word.toString());
					word.setLength(0);
					started = false;
				}
			}
			else {
				word.append(c);
				started = true;
			}
		}
		if (escaped || quote != 0) {
			throw new IllegalArgumentException("Unterminated quote or escape in CLI command");
		}
		if (started) {
			result.add(word.toString());
		}
		if (result.isEmpty() || result.get(0).isEmpty()) {
			throw new IllegalArgumentException("CLI command needs an executable");
		}
		return result;
	}

	@Override
	public ConnectionTarget getTarget() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return getLocalHost();
	}

	@Override
	public int getRemotePort() {
		return getLocalPort();
	}

	private boolean reachable() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(getLocalHost(), getLocalPort()), 150);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public synchronized void connect() {
		disconnect();
		List<String> args = parse(command);
		System.err.println("Creating CLI tunnel for " + getLocalHost() + ":" + getLocalPort());
		if (getLocalPort() < 1 || getLocalPort() > 65535) {
			throw new IllegalArgumentException("CLI endpoint port must be 1-65535");
		}
		// Fail closed if another listener already owns the endpoint before launching anything.
		try (ServerSocket check = new ServerSocket()) {
			check.bind(new InetSocketAddress(getLocalHost(), getLocalPort()));
		}
		catch (IOException e) {
			throw new IllegalStateException("CLI endpoint already occupied or cannot be bound: " + getLocalHost() + ":" + getLocalPort(), e);
		}
		try {
			synchronized (output) {
				output.setLength(0);
			}
			process = new ProcessBuilder(args).redirectErrorStream(true).start();
			final Process owned = process;
			Thread drain = new Thread(() -> {
				try (InputStream stream = owned.getInputStream()) {
					byte[] bytes = new byte[512];
					int read;
					while ((read = stream.read(bytes)) != -1) {
						synchronized (output) {
							output.append(new String(bytes, 0, read, StandardCharsets.UTF_8));
							if (output.length() > 2048) {
								output.delete(0, output.length() - 2048);
							}
						}
					}
				}
				catch (IOException e) {
					logger.debug("CLI tunnel output stream closed", e);
				}
			}, "cli-tunnel-output");
			drain.setDaemon(true);
			drain.start();
			shutdownHook = new Thread(() -> stopProcess(owned), "cli-tunnel-shutdown");
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
			while (System.nanoTime() < deadline) {
				if (!owned.isAlive()) {
					throw new IllegalStateException("CLI tunnel exited (code " + owned.exitValue() + "): " + diagnostics());
				}
				if (reachable()) {
					if (!owned.isAlive()) {
						throw new IllegalStateException("CLI tunnel exited before endpoint was ready: " + diagnostics());
					}
					System.err.println("CLI tunnel endpoint ready at " + getLocalHost() + ":" + getLocalPort());
					return;
				}
				Thread.sleep(100);
			}
			throw new IllegalStateException("CLI endpoint not ready within 10 seconds: " + diagnostics());
		}
		catch (IOException e) {
			disconnect();
			throw new IllegalStateException("Unable to launch CLI tunnel executable", e);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			disconnect();
			throw new IllegalStateException("CLI tunnel startup interrupted", e);
		}
		catch (RuntimeException e) {
			disconnect();
			throw e;
		}
	}

	private String diagnostics() {
		synchronized (output) {
			return output.toString().replaceAll("[\\r\\n]+", " ");
		}
	}

	@Override
	public synchronized void disconnect() {
		if (shutdownHook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			}
			catch (IllegalStateException e) {
				logger.debug("JVM shutdown already in progress", e);
			}
			shutdownHook = null;
		}
		if (process != null) {
			stopProcess(process);
			process = null;
		}
	}

	private static void stopProcess(Process owned) {
		List<Object> descendants = new ArrayList<Object>();
		Method destroy = null, destroyForcibly = null, isAlive = null;
		try {
			// ProcessHandle is absent on Java 8; never resolve it as a static dependency.
			Class<?> handle = Class.forName("java.lang.ProcessHandle");
			Method toHandle = Process.class.getMethod("toHandle");
			Method children = handle.getMethod("descendants");
			destroy = handle.getMethod("destroy");
			destroyForcibly = handle.getMethod("destroyForcibly");
			isAlive = handle.getMethod("isAlive");
			try (Stream<?> stream = (Stream<?>) children.invoke(toHandle.invoke(owned))) {
				stream.forEach(descendants::add);
			}
		}
		catch (ClassNotFoundException e) {
			logger.debug("Descendant cleanup unavailable on Java 8", e);
		}
		catch (ReflectiveOperationException | SecurityException e) {
			logger.warn("Unable to discover CLI tunnel descendants; stopping parent only", e);
			descendants.clear();
		}
		long gracefulDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
		for (Object child : descendants) {
			try {
				destroy.invoke(child);
			}
			catch (ReflectiveOperationException | SecurityException e) {
				logger.warn("Unable to stop CLI tunnel descendant", e);
			}
		}
		owned.destroy();
		if (waitForExit(owned, descendants, isAlive, gracefulDeadline)) {
			return;
		}
		long forcedDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
		for (Object child : descendants) {
			try {
				if ((Boolean) isAlive.invoke(child)) {
					destroyForcibly.invoke(child);
				}
			}
			catch (ReflectiveOperationException | SecurityException e) {
				logger.warn("Unable to force-stop CLI tunnel descendant", e);
			}
		}
		if (owned.isAlive()) {
			owned.destroyForcibly();
		}
		if (!Thread.currentThread().isInterrupted() && !waitForExit(owned, descendants, isAlive, forcedDeadline)) {
			logger.warn("CLI tunnel processes did not exit before cleanup deadline");
		}
	}

	private static boolean waitForExit(Process owned, List<Object> descendants, Method isAlive, long deadline) {
		while (true) {
			boolean running = owned.isAlive();
			for (Object child : descendants) {
				try {
					running |= (Boolean) isAlive.invoke(child);
				}
				catch (ReflectiveOperationException | SecurityException e) {
					logger.warn("Unable to check CLI tunnel descendant", e);
				}
			}
			if (!running) {
				return true;
			}
			long remaining = deadline - System.nanoTime();
			if (remaining <= 0) {
				return false;
			}
			try {
				TimeUnit.NANOSECONDS.sleep(Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(25)));
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.warn("Interrupted while stopping CLI tunnel", e);
				return false;
			}
		}
	}

	@Override
	public synchronized boolean isConnected() {
		return process != null && process.isAlive() && reachable();
	}
}
