package com.provframework.api.sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.stereotype.Component;

import com.provframework.build.java.Activity;
import com.provframework.build.java.Agent;
import com.provframework.build.java.Bundle;
import com.provframework.build.java.Entity;

@Component
public class SparqlRepository {
    public List<String> getLabels(IRI type, RepositoryConnection connection) {
        String query = """
            PREFIX prov: <http://www.w3.org/ns/prov#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            SELECT ?label WHERE {
                ?entity rdf:type %s ;
                    rdfs:label ?label .
            }
            ORDER BY ?label
        """.formatted(PROV.PREFIX + ":" + type.getLocalName());

        try (TupleQueryResult result = connection.prepareTupleQuery(query).evaluate()) {
            return QueryResults.stream(result)
                .map(bs -> bs.getValue("label").stringValue())
                .collect(Collectors.toList());
        }
    }

    public Bundle getNeighbors(String label, IRI type, RepositoryConnection connection, String myNamespace) {
        ParsedIRI parsedIri = ParsedIRI.create(label);
        String encodedLabel = myNamespace + parsedIri.toString();
        int queryDepth = 5;

        // Future: update query depth to be dynamic
        String query = """
            PREFIX prov: <http://www.w3.org/ns/prov#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            SELECT * WHERE {
            <%s> rdf:type <%s> ; 
                ?p0 ?o1 .
                OPTIONAL { 
                    ?o1 ?p1 ?o2 . 
                        OPTIONAL { 
                            ?o2 ?p2 ?o3 . 
                                OPTIONAL { 
                                    ?o3 ?p3 ?o4 .
                                        OPTIONAL { 
                                            ?o4 ?p4 ?o5 . 
                                        }
                                }
                        }
                }
            }        
        """.formatted(encodedLabel, type.toString());

        Bundle bundle = new Bundle();

        // Capture the subject, predicate, and set of objects so we can build the Bundle
        Map<String, Map<String, Set<String>>> statementMap = new HashMap<>();
        try (TupleQueryResult result = connection.prepareTupleQuery(query).evaluate()) {
            QueryResults.stream(result).forEach(bs -> {  // Table row iteration
                for (int i = 0; i < queryDepth; i++) {  // Table column iteration
                    String sKey = "o" + i;
                    String pKey = "p" + i;
                    String oKey = "o" + (i + 1);

                    // Query uses optionals, exit loop once we have run out of columns
                    if (!bs.hasBinding(pKey) || !bs.hasBinding(oKey)) {
                        break;
                    }

                    String subject;
                    if (i == 0) {
                        subject = encodedLabel;
                    } else {
                        subject = bs.getBinding(sKey).getValue().stringValue();
                    }

                    String predicate = bs.getBinding(pKey).getValue().stringValue();
                    String object = bs.getBinding(oKey).getValue().stringValue();

                    // Create subject IRI entry if it doesn't exist
                    statementMap.putIfAbsent(subject, new HashMap<>());

                    // Get predicate map for subject
                    Map<String, Set<String>> predicates = statementMap.get(subject);

                    // Create predicate entry if it doesn't exist
                    predicates.putIfAbsent(predicate, new HashSet<>());

                    // Add object to predicate set
                    predicates.get(predicate).add(object);
                }
            });
        }

        // Update bundle from statement map
        for (String uri : statementMap.keySet()) {
            Map<String, Set<String>> predicates = statementMap.get(uri);
            String objectType = predicates.get(RDF.TYPE.stringValue()).iterator().next();
            String objectId = predicates.get(RDFS.LABEL.stringValue()).iterator().next();
            if (objectType.equals(PROV.ENTITY.stringValue())) {
                Entity entity = new Entity();
                entity.setId(objectId);

                predicates.forEach((predicate, objectSet) -> {
                    if (predicate.equals(PROV.WAS_DERIVED_FROM.toString())) {
                        objectSet.forEach(objectValue -> {
                            entity.addWasDerivedFrom(statementMap.get(objectValue).get(RDFS.LABEL.stringValue()).iterator().next());
                        });
                    } else if (predicate.equals(PROV.WAS_GENERATED_BY.toString())) {
                        objectSet.forEach(objectValue -> {
                            entity.addWasGeneratedBy(statementMap.get(objectValue).get(RDFS.LABEL.stringValue()).iterator().next());
                        });
                    } else if (predicate.equals(PROV.WAS_ATTRIBUTED_TO.toString())) {
                        objectSet.forEach(objectValue -> {
                            entity.addWasAttributedTo(statementMap.get(objectValue).get(RDFS.LABEL.stringValue()).iterator().next());
                        });
                    }
                });
                bundle.addEntity(entity);
            } else if (objectType.equals(PROV.ACTIVITY.stringValue())) {
                Activity activity = new Activity();
                activity.setId(objectId);

                Set<String> startedAtTimeSet = predicates.get(PROV.STARTED_AT_TIME.stringValue());
                if (startedAtTimeSet != null && !startedAtTimeSet.isEmpty()) {
                    activity.setStartedAtTime(startedAtTimeSet.iterator().next());
                }

                Set<String> endedAtTimeSet = predicates.get(PROV.ENDED_AT_TIME.stringValue());
                if (endedAtTimeSet != null && !endedAtTimeSet.isEmpty()) {
                    activity.setEndedAtTime(endedAtTimeSet.iterator().next());
                }

                Set<String> atLocationSet = predicates.get(PROV.AT_LOCATION.stringValue());
                if (atLocationSet != null && !atLocationSet.isEmpty()) {
                    activity.setAtLocation(atLocationSet.iterator().next());
                }

                predicates.forEach((predicate, objectSet) -> {
                    if (predicate.equals(PROV.WAS_INFORMED_BY.toString())) {
                        objectSet.forEach(objectValue -> {
                            activity.addWasInformedBy(statementMap.get(objectValue).get(RDFS.LABEL.stringValue()).iterator().next());
                        });
                    } else if (predicate.equals(PROV.USED.toString())) {
                        objectSet.forEach(objectValue -> {
                            activity.addUsed(statementMap.get(objectValue).get(RDFS.LABEL.stringValue()).iterator().next());
                        });
                    } else if (predicate.equals(PROV.WAS_ASSOCIATED_WITH.toString())) {
                        objectSet.forEach(objectValue -> {
                            activity.addWasAssociatedWith(statementMap.get(objectValue).get(RDFS.LABEL.stringValue()).iterator().next());
                        });
                    }
                });
                bundle.addActivity(activity);
            } else if (objectType.equals(PROV.AGENT.stringValue())) {
                Agent agent = new Agent();
                agent.setId(objectId);

                predicates.forEach((predicate, objectSet) -> {
                    if (predicate.equals(PROV.ACTED_ON_BEHALF_OF.toString())) {
                        objectSet.forEach(objectValue -> {
                            agent.addActedOnBehalfOf(statementMap.get(objectValue).get(RDFS.LABEL.stringValue()).iterator().next());
                        });
                    }
                });
                bundle.addAgent(agent);
            }
        }
        
        return bundle;
    }
}
