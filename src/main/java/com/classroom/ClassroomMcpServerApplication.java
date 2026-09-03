package com.classroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application class for the Classroom MCP Server.
 * Exposes tool management via stdio transport for Claude Desktop integration.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.classroom"})
public class ClassroomMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassroomMcpServerApplication.class, args);
    }
}
