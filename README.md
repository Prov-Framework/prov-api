# prov-api

## Description
An API for retrieving provenance data from a graph database using Cypher, Gremlin, or SPARQL.

It is not meant to be run as-is. The intent is that you clone or fork and modify this to meet your requirements based on the graph database decisions that you have already made, or have been made for you. For example, don't carry gremlin dependencies into production if you intend to use SPARQL. Un-needed dependencies increase the number of vulnerabilities scanning tools will find in your image. prov-capture is intentionally not packaged as a docker container for this very reason. Delete things you don't need, modify database access configs for your situation (Neo4j and AWS Neptune have differences), use base images to create your container that meet your organizations acceptance criteria.

## In-Scope Features
- Covering a base implementation of the three major query languages, Cypher/OpenCypher, Gremlin, SPARQL.
- Eventually adopting ISO standard GQL as Neo4j updates Cypher DSLs
- Testing for performance with major providers (Neo4j, JanusGraph, AWS Neptune, Cosmos DB, etc)
- Unit Test Coverage
- SonarQube Maintenance (As able. This is not a paid effort. If you need fixes faster you should do them yourself to your clone/fork.)

## Out-of-Scope Features
- Other graph query languages (ArrangoDB, TigerGraph, Oracle, etc). Now that the ISO standard has come out with GQL, the goal is for this project to support one declarative property graph language (Right now OpenCypher but eventually GQL), an imperative property graph query language (Gremlin), and a declarative semantic graph language (SPARQL). This is subject to change, but every implementation comes with a maintenance tail without increased time by maintainers.
- SSL/Authentication configurations for all possible scenarios. There are just too many vendors and each organization has different requirements.
- Container/Image packaging. Each organization has different security requirements and devs should modify thier fork before creating a production image.

### TODO for 0.1.0 Release
- [ ] Complete base Cypher implementation to list entities, activities, and agents
- [ ] Complete base Gremlin implementation to list entities, activities, and agents
- [ ] Complete base Cypher implementation to get neighbors subgraph
- [ ] Complete base Gremlin implementation to get neighbors subgraph
- [ ] Address all critical and high Sonar findings
- [ ] Achieve 80% code coverage

### TODO for 0.2.0 Release
- [ ] Ensure proper error response handling and null checks
- [ ] Address 100% sonar findings
- [ ] Acheive 100% code coverage

### TODO for 0.3.0 Release
- [ ] Performance testing and document results for Neptune: Cypher (AWS Deployment)
- [ ] Performance testing and document results for Neptune: Gremlin (AWS Deployment)
- [ ] Performance testing and document results for Neptune: SPARQL (AWS Deployment)
- [ ] Performance testing and document results for Neo4j (AWS Deployment)
- [ ] Performance testing and document results for JanusGraph (AWS Deployment)
- [ ] Performance testing and document results for RDF4j Server (AWS Deployment)
- [ ] Performance testing and document results for Jena Fuseki (AWS Deployment)
- [ ] Performance testing and document results for Comsmos DB (Azure Deployment)

### TODO for 1.0.0 Release
- [ ] Ensure graceful failure of DB connection issues with auto-reconnect behavior 

### TODO for 1.1.0 Release
- [ ] Add full PROV-O terminology support