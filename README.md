# VDSM Java API example appliaction

This shows how to connect to a VDSM instance using the JSON-RPC api and send
commands

There will more more stuff when more features are added to the Java Bindings

The program recieves an argument in the form of tcp://<ip>:<port>.

It will print the VDSM instance UUID when successful or an error when failing.

# How to install the json-rpc support
You need the latest sources from the head of the
[Java Bindings gerrit topic][json_rpc_r]

Go to the root of the VDSM source tree and create the RPMS:

```bash
$ ./autogen.sh --system
$ ./configure
$ make rpm
```

Install apart from the regular VDSM rpms:
- vdsm-jsonrpc
- vdsm-yajsonrpc

Restart VDSM and you are all set

# How to get the Java Bindings
You need the latest sources from the head of the
[Java Bindings gerrit topic][json_rpc_r]

Go to the root of the VDSM source tree and do the following to install the Java
Bindings in your maven package cache:
```bash
$ cd client/java
$ mvn install
```

[json_rpc_r]: http://gerrit.ovirt.org/#/q/status:open+project:vdsm+branch:master+topic:json_rpc_r,n,z "json_rpc_r"
