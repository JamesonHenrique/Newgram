package com.jhcs.newgram.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class OpenApiConfig {

    private final Environment environment;

    @Value("${spring.application.name:Newgram}")
    private String applicationName;

    public OpenApiConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        boolean isSecurityEnabled = !Arrays.asList(environment.getActiveProfiles()).contains("basic");

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version("1.0.0")
                        .description("API para Newgram, uma aplicação de rede social inspirada no Instagram.")
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("Jameson Henrique")
                                .url("https://github.com/JamesonHenrique")
                                .email("jamesonhenrique14@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação adicional")
                        .url("https://example.com/docs"))
                .servers(getServers())
                .tags(getApplicationTags());

        if (isSecurityEnabled) {
            openAPI.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                    .components(new Components()
                            .addSecuritySchemes("bearerAuth", createJwtSecurityScheme())
                            .addSecuritySchemes("refreshToken", createRefreshTokenSecurityScheme()));
        }

        return openAPI;
    }

    private List<Server> getServers() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Servidor de desenvolvimento");



        return Collections.singletonList(localServer);
    }

    private List<Tag> getApplicationTags() {
        return Arrays.asList(
                new Tag().name("Autenticação").description("Operações relacionadas à autenticação"),
                new Tag().name("Usuários").description("Gerenciamento de usuários"),
                new Tag().name("Posts").description("Operações com posts"),
                new Tag().name("Comentários").description("Gerenciamento de comentários"),
                new Tag().name("Stories").description("Operações com stories"),
                new Tag().name("Mensagens").description("Sistema de mensagens"),
                new Tag().name("Arquivos").description("Upload e download de arquivos")
        );
    }

    private SecurityScheme createJwtSecurityScheme() {
        return new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Insira um token JWT válido aqui. Expira em 24h.");
    }

    private SecurityScheme createRefreshTokenSecurityScheme() {
        return new SecurityScheme()
                .name("refreshToken")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Insira um refresh token válido para obter novos tokens. Validade de 7 dias.");
    }
}