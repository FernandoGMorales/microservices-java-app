package com.example.microservicesjavaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Microservices Java Application.
 * This class uses Spring Boot's @SpringBootApplication annotation,
 * which is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions for the application context.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings,
 * other beans, and various property settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services
 * in the 'com.example.microservicesjavaapp' package, allowing it to discover controllers, services, etc.
 *
 * The @EnableAsync annotation enables Spring's asynchronous method execution capability,
 * which will be crucial for processing orders asynchronously as per the requirements.
 */
@SpringBootApplication
@EnableAsync // Enables asynchronous method execution
public class MicroservicesJavaAppApplication {

    /**
     * The main method that starts the Spring Boot application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(MicroservicesJavaAppApplication.class, args);
    }

}