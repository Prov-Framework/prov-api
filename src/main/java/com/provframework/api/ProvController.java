package com.provframework.api;

import java.util.List;

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

    @GetMapping("entities")
    public List<String> listEntities() {
        return this.sparqlDriver.getEntities();
    }
}
