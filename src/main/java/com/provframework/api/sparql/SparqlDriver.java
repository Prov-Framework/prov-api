package com.provframework.api.sparql;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.provframework.build.java.Bundle;

@Service
public class SparqlDriver {
    @SuppressWarnings("unused")
    private final String uri;
    @SuppressWarnings("unused")
    private final String myNamespace;
    @SuppressWarnings("unused")
    private String myPrefix;
    private final RepositoryConnection connection;
    private final SparqlRepository repository;

    public SparqlDriver(@Value("${sparql.uri}") String uri, @Value("${sparql.mynamespace}") String myNamespace,
                @Value("${sparql.myprefix}") String myPrefix, SparqlRepository repository) {
        this.uri = uri;
        this.myNamespace = myNamespace;
        this.myPrefix = myPrefix;
        this.repository = repository;
        this.connection = new SPARQLRepository(uri, uri).getConnection();
    }

    public List<String> getLabels(IRI type) {
        return this.repository.getLabels(type, this.connection);
    }

    public Bundle getNeighbors(String label, IRI type) {
        return this.repository.getNeighbors(label, type, this.connection, this.myNamespace);
    }
}