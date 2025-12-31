package com.provframework.api;

import java.util.List;

import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.provframework.api.sparql.SparqlDriver;

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
}
