package com.plataforma.conversacional.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class StorageConfig {

    @Bean
    public Path uploadDir(@Value("${app.storage.upload-dir}") String uploadDir) {
        return Path.of(uploadDir).toAbsolutePath().normalize();
    }
}
