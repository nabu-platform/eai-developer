package be.nabu.eai.developer.impl;

import static org.junit.Assert.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class CLIConnectionTunnelTest {
	@Test
	public void parsesQuotedArgumentsWithoutShell() {
		assertEquals(Arrays.asList("/path with spaces/ssh", "-L", "127.0.0.1:10021:localhost:21", "host name", ""),
			CLIConnectionTunnel.parse("\"/path with spaces/ssh\" -L 127.0.0.1:10021:localhost:21 'host name' \"\""));
		assertEquals(Arrays.asList("C:\\Program Files\\ssh.exe", "host name"),
			CLIConnectionTunnel.parse("\"C:\\Program Files\\ssh.exe\" host\\ name"));
		try {
			CLIConnectionTunnel.parse("ssh 'unfinished");
			fail("Expected invalid quoting");
		}
		catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("quote"));
		}
	}

	@Test
	public void refusesOccupiedPortWithoutStartingCommand() throws Exception {
		try (ServerSocket server = new ServerSocket(0)) {
			CLIConnectionTunnel tunnel = new CLIConnectionTunnel("missing-executable", "127.0.0.1", server.getLocalPort());
			try {
				tunnel.connect();
				fail("Expected occupied endpoint");
			}
			catch (IllegalStateException expected) {
				assertTrue(expected.getMessage().contains("occupied"));
			}
		}
	}

	public static class Listener {
		public static void main(String[] args) throws Exception {
			try (ServerSocket socket = new ServerSocket(Integer.parseInt(args[0]))) {
				while (true) {
					socket.accept().close();
				}
			}
		}
	}

	private static int freePort() throws Exception {
		try (ServerSocket server = new ServerSocket(0)) {
			return server.getLocalPort();
		}
	}

	private static String javaCommand(Class<?> main, int port) {
		String executable = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		return "\"" + executable + "\" -cp \"" + System.getProperty("java.class.path") + "\" " + main.getName() + " " + port;
	}

	public static class Sleeper {
		public static void main(String[] args) throws Exception {
			Thread.sleep(30000);
		}
	}

	public static class Launcher {
		public static void main(String[] args) throws Exception {
			for (int i = 0; i < 6; i++) {
				new ProcessBuilder(CLIConnectionTunnel.parse(javaCommand(Sleeper.class, 0))).inheritIO().start();
			}
			new ProcessBuilder(CLIConnectionTunnel.parse(javaCommand(Listener.class, Integer.parseInt(args[0])))).inheritIO().start();
			Thread.sleep(30000);
		}
	}

	public static class HookOwner {
		public static void main(String[] args) throws Exception {
			int port = Integer.parseInt(args[0]);
			new CLIConnectionTunnel(javaCommand(Listener.class, port), "127.0.0.1", port).connect();
			System.out.println("READY");
			System.out.flush();
			System.exit(0);
		}
	}

	@Test
	public void disconnectStopsDiscoverableDescendant() throws Exception {
		if (System.getProperty("java.specification.version").equals("1.8")) {
			return;
		}
		int port = freePort();
		CLIConnectionTunnel tunnel = new CLIConnectionTunnel(javaCommand(Launcher.class, port), "127.0.0.1", port);
		Process unrelated = new ProcessBuilder(CLIConnectionTunnel.parse(javaCommand(Sleeper.class, 0))).start();
		try {
			tunnel.connect();
			assertTrue(tunnel.isConnected());
			long start = System.nanoTime();
			tunnel.disconnect();
			assertTrue("Cleanup exceeded shared deadline", TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) < 6);
			assertTrue("Unrelated process was terminated", unrelated.isAlive());
			try (ServerSocket server = new ServerSocket(port)) {
				assertTrue(server.isBound());
			}
		}
		finally {
			tunnel.disconnect();
			unrelated.destroyForcibly();
		}
	}

	@Test
	public void shutdownHookStopsTunnel() throws Exception {
		int port = freePort();
		Process owner = new ProcessBuilder(CLIConnectionTunnel.parse(javaCommand(HookOwner.class, port))).redirectErrorStream(true).start();
		try {
			assertTrue("Hook owner failed to exit", owner.waitFor(15, TimeUnit.SECONDS));
			assertEquals(0, owner.exitValue());
			boolean ready = false;
			try (BufferedReader output = new BufferedReader(new InputStreamReader(owner.getInputStream(), "UTF-8"))) {
				String line;
				while ((line = output.readLine()) != null) {
					if (line.equals("READY")) {
						ready = true;
					}
				}
			}
			assertTrue("Hook owner did not report readiness", ready);
			try (ServerSocket server = new ServerSocket(port)) {
				assertTrue(server.isBound());
			}
		}
		finally {
			owner.destroyForcibly();
		}
	}

	@Test
	public void disconnectAndReconnectReleaseOwnedEndpoint() throws Exception {
		int port;
		try (ServerSocket server = new ServerSocket(0)) {
			port = server.getLocalPort();
		}
		String executable = System.getProperty("java.home") + "/bin/java";
		String command = "\"" + executable + "\" -cp \"" + System.getProperty("java.class.path") + "\" " + Listener.class.getName() + " " + port;
		CLIConnectionTunnel tunnel = new CLIConnectionTunnel(command, "127.0.0.1", port);
		try {
			tunnel.connect();
			assertTrue(tunnel.isConnected());
			try (Socket client = new Socket("127.0.0.1", port)) {
				assertTrue(client.isConnected());
			}
			tunnel.connect();
			assertTrue(tunnel.isConnected());
		}
		finally {
			tunnel.disconnect();
		}
		assertFalse(tunnel.isConnected());
		try (ServerSocket server = new ServerSocket(port)) {
			assertTrue(server.isBound());
		}
	}

	@Test
	public void cleansUpFailedStartupAndReconnect() throws Exception {
		int port;
		try (ServerSocket server = new ServerSocket(0)) {
			port = server.getLocalPort();
		}
		CLIConnectionTunnel tunnel = new CLIConnectionTunnel("java -version", "127.0.0.1", port);
		try {
			tunnel.connect();
			fail("Expected early exit");
		}
		catch (IllegalStateException expected) {
			assertFalse(tunnel.isConnected());
		}
		finally {
			tunnel.disconnect();
		}
	}
}
