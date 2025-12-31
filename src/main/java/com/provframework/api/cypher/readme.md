# Cypher

## Description
This folder contains all the code needed to use Cypher. Specifically there is a database driver and a language class. The language class builds the bundle insert statement. The driver connects to a remote graph database using the Neo4j Bolt library. Currently the default configuration is for a Neo4j docker container running on a local machine.

### How to clear a cypher DB (for testing)
MATCH (n) DETACH DELETE n;