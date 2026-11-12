# CLI tunnels

Select the CLI protocol in a server profile. Set Server and Port to the local endpoint that Developer will connect to after the tunnel starts (for example `127.0.0.1` and `10021`). Set CLI command to a foreground command such as:

```
ssh -N -o ExitOnForwardFailure=yes -L 127.0.0.1:10021:localhost:21 nabu-prd1
```

The command runs directly through ProcessBuilder, not a shell. Whitespace separates arguments; single or double quotes group paths or arguments with spaces, and a backslash before whitespace, a quote, or another backslash escapes that character. There is no pipe, redirect, glob, variable expansion or implicit shell. To run a script, invoke its executable path or an explicit interpreter (for example `bash /path/to/script.sh`). Keep the tunnel command and any wrapper scripts in the foreground; scripts must not daemonize or detach their tunnel process. The system SSH executable uses its own `~/.ssh/config`, including ProxyCommand, without Developer parsing it.

The endpoint must be free before startup; Developer waits up to 10 seconds for it to accept connections. Endpoint readiness means TCP reachability, not cryptographic proof that the launched process owns the listener; another process that binds the port during startup can cause a false ready result. Use a fixed port matching the command's forwarding port. The CLI process is stopped when Developer exits or reconnects. On Java 9+ Developer also attempts to stop discoverable descendants, with shared graceful and forced cleanup deadlines. On Java 8 only the launched parent can be stopped; child processes may survive. Detached or reparented processes are unsupported and may outlive Developer. Command output is retained in a short diagnostic tail for startup errors; avoid putting secrets in the command output.
