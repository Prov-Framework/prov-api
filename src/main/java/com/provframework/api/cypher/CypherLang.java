package com.provframework.api.cypher;

import com.provframework.api.StreamUtils;
import com.provframework.build.java.Activity;
import com.provframework.build.java.Agent;
import com.provframework.build.java.Bundle;
import com.provframework.build.java.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.StatementBuilder.OngoingMerge;
import org.neo4j.cypherdsl.core.PatternElement;
import org.neo4j.cypherdsl.core.Relationship;
import org.springframework.stereotype.Component;

@Component
public class CypherLang {

    public Statement getInsertStatement(Bundle bundle) {
        List<PatternElement> nodes = new ArrayList<>();
        List<PatternElement> relationships = new ArrayList<>();

        StreamUtils.getNonNullStream(bundle.getEntities()).forEach(entity -> insertEntity(nodes, relationships, entity));
        StreamUtils.getNonNullStream(bundle.getActivities()).forEach(activity -> insertActivity(nodes, relationships, activity));
        StreamUtils.getNonNullStream(bundle.getAgents()).forEach(agent -> insertAgent(nodes, relationships, agent));

        OngoingMerge statement = Cypher.merge(nodes.get(0));

        // Nodes must be merged before relationships or else node properties will be lost
        nodes.stream().skip(1).forEach(statement::merge);
        relationships.stream().forEach(statement::merge);

        return statement.build();
    }

    private void insertEntity(List<PatternElement> nodes, List<PatternElement> relationships, Entity entity) {
        Node entityNode = createNode(entity.getId(), PROV.ENTITY, null);
        nodes.add(entityNode);

        addRelationships(relationships, entityNode, PROV.WAS_DERIVED_FROM, PROV.ENTITY, entity.getWasDerivedFrom());
        addRelationships(relationships, entityNode, PROV.WAS_ATTRIBUTED_TO, PROV.AGENT, entity.getWasAttributedTo());
        addRelationships(relationships, entityNode, PROV.WAS_GENERATED_BY, PROV.ACTIVITY, entity.getWasGeneratedBy());
    }

    private void insertActivity(List<PatternElement> nodes, List<PatternElement> relationships, Activity activity) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(RDFS.LABEL.getLocalName(), Cypher.literalOf(activity.getId()));

        // Optional Properties
        if(activity.getStartedAtTime() != null) {
            properties.put(PROV.STARTED_AT_TIME.getLocalName(), Cypher.literalOf(activity.getStartedAtTime()));
        }
        if(activity.getEndedAtTime() != null) {
            properties.put(PROV.ENDED_AT_TIME.getLocalName(), Cypher.literalOf(activity.getEndedAtTime()));
        }
        if(activity.getAtLocation() != null) {
            properties.put(PROV.AT_LOCATION.getLocalName(), Cypher.literalOf(activity.getAtLocation()));
        }

        Node activityNode = createNode(activity.getId(), PROV.ACTIVITY, properties);
        nodes.add(activityNode);

        addRelationships(relationships, activityNode, PROV.WAS_ASSOCIATED_WITH, PROV.AGENT, activity.getWasAssociatedWith());
        addRelationships(relationships, activityNode, PROV.USED, PROV.ENTITY, activity.getUsed());
        addRelationships(relationships, activityNode, PROV.WAS_INFORMED_BY, PROV.ACTIVITY, activity.getWasInformedBy());
    }

    private void insertAgent(List<PatternElement> nodes, List<PatternElement> relationships, Agent agent) {
        Node agentNode = createNode(agent.getId(), PROV.AGENT, null);
        nodes.add(agentNode);

        addRelationships(relationships, agentNode, PROV.ACTED_ON_BEHALF_OF, PROV.AGENT, agent.getActedOnBehalfOf());
    }

    private void addRelationships(List<PatternElement> relationships, Node primaryNode,
        IRI edgeLabel, IRI nodeType, List<String> relatedNodeLabels) {
        StreamUtils.getNonNullStream(relatedNodeLabels)
        .forEach(relatedNode -> {
            Node secondaryNode = createNode(relatedNode, nodeType, null);
            Relationship relationship = primaryNode.relationshipTo(secondaryNode, edgeLabel.getLocalName());
            relationships.add(relationship);
        });
    }

    private Node createNode(String id, IRI type, Map<String, Object> properties) {
        Node node = Cypher.node(type.getLocalName()).named(id);

        if (properties != null) {
            node = node.withProperties(properties);
        } else {
            node = node.withProperties(
                RDFS.LABEL.getLocalName(), Cypher.literalOf(id)
            );
        }

        return node;
    }
}