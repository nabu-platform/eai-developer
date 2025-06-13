Add to command line:

-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:8888,server=y,suspend=n

In eclipse, add a "debug configuration" and select (at the bottom) remote application, set port to 8888.