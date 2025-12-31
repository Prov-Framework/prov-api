package com.provframework.api.gremlin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.Merge;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.springframework.stereotype.Component;

import com.provframework.api.StreamUtils;
import com.provframework.build.java.Activity;
import com.provframework.build.java.Agent;
import com.provframework.build.java.Bundle;
import com.provframework.build.java.Entity;

@Component
public class GremlinLang {
    public GraphTraversalSource getInsertStatement(Bundle bundle, GraphTraversalSource g) {
        StreamUtils.getNonNullStream(bundle.getEntities()).forEach(entity -> insertEntity(g, entity));
        StreamUtils.getNonNullStream(bundle.getActivities()).forEach(activity -> insertActivity(g, activity));
        StreamUtils.getNonNullStream(bundle.getAgents()).forEach(agent -> insertAgent(g, agent));

        return g;
    }

    private void insertEntity(GraphTraversalSource g, Entity entity) {
        Vertex entityVertex = g.mergeV(Map.of(
            T.label, PROV.ENTITY.getLocalName(),
            "name", entity.getId()
        )).next();

        addRelatedVerticies(g, entityVertex, PROV.WAS_DERIVED_FROM, PROV.ENTITY, entity.getWasDerivedFrom());
        addRelatedVerticies(g, entityVertex, PROV.WAS_ATTRIBUTED_TO, PROV.AGENT, entity.getWasAttributedTo());
        addRelatedVerticies(g, entityVertex, PROV.WAS_GENERATED_BY, PROV.ACTIVITY, entity.getWasGeneratedBy());
    }

    private void insertActivity(GraphTraversalSource g, Activity activity) {
        Map<Object, Object> optionalProperties = new HashMap<>();
        if(activity.getStartedAtTime() != null) {
            optionalProperties.put(PROV.STARTED_AT_TIME.getLocalName(), activity.getStartedAtTime());
        }
        if(activity.getEndedAtTime() != null) {
            optionalProperties.put(PROV.ENDED_AT_TIME.getLocalName(), activity.getEndedAtTime());
        }
        if(activity.getAtLocation() != null) {
            optionalProperties.put(PROV.AT_LOCATION.getLocalName(), activity.getAtLocation());
        }

        Vertex activityVertex = g.mergeV(Map.of( // Match on label and name
            T.label, PROV.ACTIVITY.getLocalName(),
            "name", activity.getId()
        )).option(Merge.onMatch, optionalProperties).next();

        addRelatedVerticies(g, activityVertex, PROV.WAS_ASSOCIATED_WITH, PROV.AGENT, activity.getWasAssociatedWith());
        addRelatedVerticies(g, activityVertex, PROV.USED, PROV.ENTITY, activity.getUsed());
        addRelatedVerticies(g, activityVertex, PROV.WAS_INFORMED_BY, PROV.ACTIVITY, activity.getWasInformedBy());
    }

    private void insertAgent(GraphTraversalSource g, Agent agent) {
        Vertex agentVertex = g.mergeV(Map.of(
            T.label, PROV.AGENT.getLocalName(),
            "name", agent.getId()
        )).next();

        addRelatedVerticies(g, agentVertex, PROV.ACTED_ON_BEHALF_OF, PROV.AGENT, agent.getActedOnBehalfOf());
    }

    private void addRelatedVerticies(GraphTraversalSource g, Vertex primaryVertex, IRI edgeLabel, IRI vertexType, 
        List<String> relatedVertexLabels) {
        StreamUtils.getNonNullStream(relatedVertexLabels)
        .forEach(relatedVertex -> {
            Vertex secondaryVertex = g.mergeV(Map.of(
                T.label, vertexType.getLocalName(),
                "name", relatedVertex
            )).next();
            g.mergeE(Map.of(
                T.label, edgeLabel.getLocalName(), 
                Direction.from, primaryVertex, 
                Direction.to, secondaryVertex)
            ).iterate();
        });
    }
}