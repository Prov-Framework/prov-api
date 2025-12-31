package com.provframework.api.cypher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.provframework.build.java.Bundle;

import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

@Service
public class CypherDriver {
    @SuppressWarnings("unused")
    private final String uri;
    @SuppressWarnings("unused")
    private final String username;
    @SuppressWarnings("unused")
    private final String password;
    private final Driver driver;
    private CypherLang cypherLang;

    public CypherDriver(@Value("${bolt.uri}") String uri,
                @Value("${bolt.username}") String username,
                @Value("${bolt.password}") String password,
                CypherLang cypherLang) {
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.cypherLang = cypherLang;
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void insertBundle(Bundle bundle) {
        Statement statement = cypherLang.getInsertStatement(bundle);
		driver.executableQuery(statement.getCypher()).execute();
    }
}
