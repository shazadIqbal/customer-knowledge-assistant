package com.example.auth;

import com.example.auth.ingestion.GitHubIngestionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GitHubIngestionProperties.class)
public class SpringJwtAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJwtAuthApplication.class, args);
    }
}
