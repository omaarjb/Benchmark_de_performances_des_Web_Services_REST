package com.omar.datarest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.omar")
@EntityScan(basePackages = "com.omar.entities")
@EnableJpaRepositories(basePackages = "com.omar.datarest.repositories")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("Spring Data REST running at http://localhost:8080/api");
    }
}
