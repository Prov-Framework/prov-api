package com.provframework.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.provframework.api.cypher.CypherDriver;
import com.provframework.api.gremlin.GremlinDriver;
import com.provframework.api.sparql.SparqlDriver;

@SpringBootApplication
@Component
public class Main {

	private Logger logger = LoggerFactory.getLogger(Main.class);

	private CypherDriver cypherDriver;
	private SparqlDriver sparqlDriver;
	private GremlinDriver gremlinDriver;
	private ProvController provController;

	public Main(CypherDriver cypherDriver, 
		SparqlDriver sparqlDriver, 
		GremlinDriver gremlinDriver,
		ProvController provController) {
			this.cypherDriver = cypherDriver;
			this.sparqlDriver = sparqlDriver;
			this.gremlinDriver = gremlinDriver;
			this.provController = provController;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}