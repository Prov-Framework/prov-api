package com.provframework.api;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.provframework.api.sparql.SparqlDriver;
import com.provframework.build.java.Bundle;

@RestController
@RequestMapping("/provframework")
public class ProvController {
    private final SparqlDriver sparqlDriver;

    public ProvController(SparqlDriver sparqlDriver) {
        this.sparqlDriver = sparqlDriver;
    }

    @GetMapping("list/entities")
    public List<String> listEntities() {
        return this.sparqlDriver.getLabels(PROV.ENTITY);
    }

    @GetMapping("list/activities")
    public List<String> listActivities() {
        return this.sparqlDriver.getLabels(PROV.ACTIVITY);
    }

    @GetMapping("list/agents")
    public List<String> listAgents() {
        return this.sparqlDriver.getLabels(PROV.AGENT);
    }

    @GetMapping("neighbors")
    public Bundle getNeighbors(String label, String type) {
        IRI typeIri;
        if (type.toLowerCase().equals("entity")) {
            typeIri = PROV.ENTITY;
        } else if (type.toLowerCase().equals("activity")) {
            typeIri = PROV.ACTIVITY;
        } else if (type.toLowerCase().equals("agent")) {
            typeIri = PROV.AGENT;
        } else {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        
        return this.sparqlDriver.getNeighbors(label, typeIri);
    }
}
