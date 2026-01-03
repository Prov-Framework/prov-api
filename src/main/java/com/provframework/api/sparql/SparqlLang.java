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
}