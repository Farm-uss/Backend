package com.example.practice.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

        @Value("${swagger.server-url:http://localhost:8080}")
        private String serverUrl;

        @Bean
        public OpenAPI customOpenAPI() {
                Server server = new Server();
                server.setUrl(serverUrl);
                server.setDescription("운영 서버");

                return new OpenAPI()
                        .servers(List.of(server));
        }
}