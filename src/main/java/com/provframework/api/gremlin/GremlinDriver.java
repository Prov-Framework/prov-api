package com.provframework.api.gremlin;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.util.MessageSerializer;
import org.apache.tinkerpop.gremlin.util.ser.Serializers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.provframework.build.java.Bundle;

@Service
public class GremlinDriver {
    private Logger logger = LoggerFactory.getLogger(GremlinDriver.class);

    @SuppressWarnings("unused")
    private final String uri;
    @SuppressWarnings("unused")
    private final Integer port;
    private GraphTraversalSource g;
    private GremlinLang gremlinLang;

    public GremlinDriver(@Value("${gremlin.uri}") String uri,
                @Value("${gremlin.port}") Integer port,
                GremlinLang gremlinLang) {
        this.uri = uri;
        this.port = port;
        this.gremlinLang = gremlinLang;
        
        try {
            MessageSerializer<?> serializer = Serializers.GRAPHBINARY_V1.simpleInstance(); 
            serializer.configure(
                Map.of("ioRegistries", 
                    List.of("org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry")), Map.of());

            Cluster cluster = Cluster.build()
            .addContactPoint(uri)
            .port(port)
            .serializer(serializer)
            .create(); 
            this.g = traversal().withRemote(DriverRemoteConnection.using(cluster));
        } catch (Exception e) {
            logger.error("Error initializing Gremlin driver: {}", e.getMessage());
        }
    }

    public void insertBundle(Bundle bundle) {
        try {
            this.g = gremlinLang.getInsertStatement(bundle, g);
            this.g.tx().commit();
        } catch (Exception e) {
            logger.error("Error inserting bundle in Gremlin: {}", e.getMessage());
        } finally {
            this.g.tx().close();
        }
    }
}
