package com.provframework.api.sparql;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.sparqlbuilder.core.query.InsertDataQuery;
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
    private final SparqlLang sparqlLang;

    public SparqlDriver(@Value("${sparql.uri}") String uri, @Value("${sparql.mynamespace}") String myNamespace,
                @Value("${sparql.myprefix}") String myPrefix, SparqlLang sparqlLang) {
        this.uri = uri;
        this.myNamespace = myNamespace;
        this.myPrefix = myPrefix;
        this.sparqlLang = sparqlLang;
        this.sparqlLang.setMyNamespace(myNamespace, myPrefix);
        this.connection = new SPARQLRepository(uri, uri).getConnection();
    }

    public void insertBundle(Bundle bundle) {
        InsertDataQuery statement = this.sparqlLang.getInsertStatement(bundle);
        connection.prepareUpdate(statement.getQueryString()).execute();
    }

    public List<String> getLabels(IRI type) {
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
}
