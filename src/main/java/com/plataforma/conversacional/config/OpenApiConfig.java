package com.plataforma.conversacional.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("Plataforma Conversacional API")
                        .description("API para plataforma conversacional com processamento de mensagens, documentos e pipeline RAG")
                        .version(appVersion)
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@plataforma-conversacional.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://plataforma-conversacional.com")));
    }
}
