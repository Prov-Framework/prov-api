package com.provframework.api.sparql;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.InsertDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.springframework.stereotype.Component;

import com.provframework.api.StreamUtils;
import com.provframework.build.java.Activity;
import com.provframework.build.java.Agent;
import com.provframework.build.java.Bundle;
import com.provframework.build.java.Entity;

@Component
public class SparqlLang {
    
    private static IRI provIRI = Values.iri(PROV.NAMESPACE);
    private static Prefix provPrefix = SparqlBuilder.prefix(PROV.PREFIX, provIRI);

    private static IRI rdfIRI = Values.iri(RDF.NAMESPACE);
    private static Prefix rdfPrefix = SparqlBuilder.prefix(RDF.PREFIX, rdfIRI);

    private static IRI rdfsIRI = Values.iri(RDFS.NAMESPACE);
    private static Prefix rdfsPrefix = SparqlBuilder.prefix(RDFS.PREFIX, rdfsIRI);

    private static IRI xsdIRI = Values.iri(XSD.NAMESPACE);
    private static Prefix xsdPrefix = SparqlBuilder.prefix(XSD.PREFIX, xsdIRI);

    private String myNamespace;
    private Prefix myPrefix;

    public void setMyNamespace(String myNamespace, String myPrefixString) {
        this.myNamespace = myNamespace;
        this.myPrefix = SparqlBuilder.prefix(myPrefixString, Values.iri(myNamespace));
    }

    public InsertDataQuery getInsertStatement(Bundle bundle) {
        InsertDataQuery statement = new InsertDataQuery();
        
        insertPrefixes(statement);

        StreamUtils.getNonNullStream(bundle.getEntities()).forEach(entity -> insertEntity(statement, entity));
        StreamUtils.getNonNullStream(bundle.getActivities()).forEach(activity -> insertActivity(statement, activity));
        StreamUtils.getNonNullStream(bundle.getAgents()).forEach(agent -> insertAgent(statement, agent));

        return statement;
    }

    private void insertPrefixes(InsertDataQuery statement) {
        statement.prefix(provPrefix);
        statement.prefix(myPrefix);
        statement.prefix(rdfPrefix);
        statement.prefix(rdfsPrefix);
        statement.prefix(xsdPrefix);
    }

    private void insertEntity(InsertDataQuery statement, Entity entity) {
        String entityIri = insertInstance(statement, entity.getId(), PROV.ENTITY).toString();

        addRelatedNodes(statement, entityIri, PROV.WAS_DERIVED_FROM, PROV.ENTITY, entity.getWasDerivedFrom());
        addRelatedNodes(statement, entityIri, PROV.WAS_GENERATED_BY, PROV.ACTIVITY, entity.getWasGeneratedBy());
        addRelatedNodes(statement, entityIri, PROV.WAS_ATTRIBUTED_TO, PROV.AGENT, entity.getWasAttributedTo());
    }

    private void insertActivity(InsertDataQuery statement, Activity activity) {
        String activityIri = insertInstance(statement, activity.getId(), PROV.ACTIVITY).toString();

        if (activity.getStartedAtTime() != null) {
            statement.insertData(GraphPatterns.tp(
                    Values.iri(myNamespace, activityIri),
                    PROV.STARTED_AT_TIME,
                    Values.literal(OffsetDateTime.parse(activity.getStartedAtTime()))    
            ));
        }

        if (activity.getEndedAtTime() != null) {
            statement.insertData(GraphPatterns.tp(
                    Values.iri(myNamespace, activityIri),
                    PROV.ENDED_AT_TIME,
                    Values.literal(OffsetDateTime.parse(activity.getEndedAtTime()))
            ));
        }

        if (activity.getAtLocation() != null) {
            statement.insertData(GraphPatterns.tp(
                    Values.iri(myNamespace, activityIri),
                    PROV.AT_LOCATION,
                    Values.literal(activity.getAtLocation())
            ));
        }

        addRelatedNodes(statement, activityIri, PROV.USED, PROV.ENTITY, activity.getUsed());
        addRelatedNodes(statement, activityIri, PROV.WAS_ASSOCIATED_WITH, PROV.AGENT, activity.getWasAssociatedWith());
        addRelatedNodes(statement, activityIri, PROV.WAS_INFORMED_BY, PROV.ACTIVITY, activity.getWasInformedBy());
    }

    private void insertAgent(InsertDataQuery statement, Agent agent) {
        String agentIri = insertInstance(statement, agent.getId(), PROV.AGENT).toString();
        addRelatedNodes(statement, agentIri, PROV.ACTED_ON_BEHALF_OF, PROV.AGENT, agent.getActedOnBehalfOf());
    }

    private void addRelatedNodes(InsertDataQuery statement, String primaryNodeIri, IRI edgeLabel, 
        IRI nodeType, List<String> relatedNodeLabels) {
        StreamUtils.getNonNullStream(relatedNodeLabels)
        .forEach(relatedNode -> {
            insertInstance(statement, relatedNode, nodeType);
            statement.insertData(GraphPatterns.tp(
                Values.iri(myNamespace, primaryNodeIri),
                edgeLabel,
                Values.iri(myNamespace, ParsedIRI.create(relatedNode).toString())    
            ));
        });
    }

    private ParsedIRI insertInstance(InsertDataQuery statement, String id, IRI type) {
        ParsedIRI parsedIri = ParsedIRI.create(id);

        statement.insertData(
            GraphPatterns.tp(
                Values.iri(myNamespace, parsedIri.toString()),
                RDF.TYPE,
                type    
            ),
            GraphPatterns.tp(
                Values.iri(myNamespace, parsedIri.toString()),
                RDFS.LABEL,
                Values.literal(id)
            )
        );

        return parsedIri;
    }
}