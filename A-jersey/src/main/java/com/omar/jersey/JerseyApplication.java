package com.omar.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.net.URI;

public class JerseyApplication {
    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("benchmark");
        emf.createEntityManager().close();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);

        ResourceConfig rc = new ResourceConfig()
                .packages("com.omar.jersey")
                .register(provider);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create("http://0.0.0.0:8081/api"), rc
        );

        System.out.println("Jersey running at http://localhost:8081/api");
        Thread.currentThread().join();
    }
}
