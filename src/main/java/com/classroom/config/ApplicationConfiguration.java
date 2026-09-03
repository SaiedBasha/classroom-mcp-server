package com.classroom.config;

import com.classroom.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application initialization configuration.
 * Initializes default rank levels and other setup on startup.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfiguration {
    
    private final ClassroomService classroomService;
    
    @Bean
    public CommandLineRunner initializeApplication() {
        return args -> {
            log.info("Initializing Classroom MCP Server...");
            classroomService.initializeDefaultRankLevels();
            log.info("Classroom MCP Server initialized successfully");
        };
    }
}
