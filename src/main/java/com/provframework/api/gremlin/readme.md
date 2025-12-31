# Gremlin

## Description
This folder contains all the code needed to use Gremlin. Specifically there is a database driver and a language class. The language class builds the bundle insert statement. The driver connects to a remote graph database using the Apache Tinkerpop library. Currently the default configuration is for a JanusGraph docker container running on a local machine.

### How to view inserted data on JanusGraph docker container
Exec into the container and then run:

```sh
cd /opt/janusgraph/bin
./gremlin.sh

:remote connect tinkerpop.server conf/remote.yaml
:remote console

g.V()
```

### How to clear a gremlin DB (for testing)
g.V().drop().iterate()