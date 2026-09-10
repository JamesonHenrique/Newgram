package com.jhcs.newgram.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Newgram}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components()
                .addSchemas("MultipartFile", new Schema<MultipartFile>().type("string").format("binary"))
                .addSecuritySchemes("bearerAuth", createJwtSecurityScheme())
                .addSecuritySchemes("refreshToken", createRefreshTokenSecurityScheme());

        return new OpenAPI()
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title(applicationName + " API")
                        .version("1.0.0")
                        .description("API para Newgram, uma aplicação de rede social.")
                        .contact(new Contact()
                                .name("Newgram")
                                .url("https://example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(getServers())
                .tags(getApplicationTags());
    }

    private List<Server> getServers() {
        return Arrays.asList(
                new Server().url("http://localhost:8080").description("Servidor de desenvolvimento"),
                new Server().url("https://api.newgram.example.com").description("Servidor de produção"));
    }

    private List<Tag> getApplicationTags() {
        return Arrays.asList(
                new Tag().name("Autenticação").description("Operações relacionadas à autenticação"),
                new Tag().name("Usuários").description("Gerenciamento de usuários"),
                new Tag().name("Posts").description("Operações com posts"),
                new Tag().name("Comentários").description("Gerenciamento de comentários"),
                new Tag().name("Stories").description("Operações com stories"));
    }

    private SecurityScheme createJwtSecurityScheme() {
        return new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access token JWT (expira em 15 minutos).");
    }

    private SecurityScheme createRefreshTokenSecurityScheme() {
        return new SecurityScheme()
                .name("refreshToken")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Refresh token para obter novos tokens (validade de 7 dias).");
    }
}
