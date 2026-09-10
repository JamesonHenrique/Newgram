package com.jhcs.newgram.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
                .components(new Components()
                        .addSchemas("MultipartFile", new Schema<MultipartFile>()
                                .type("string")
                                .format("binary")))

                .info(new Info()
                        .title(applicationName + " API")
                        .version("1.0.0")
                        .description("API para Newgram, uma aplicação de rede social inspirada no Instagram.")
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("Jameson Henrique")
                                .url("https://jamesonhenrique-portfolio.vercel.app/")
                                .email("jamesonhenrique14@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação adicional")
                        .url("https://example.com/docs"))
                .servers(getServers())
                .tags(getApplicationTags());

        Components components = new Components();


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
                new Tag().name("Stories").description("Operações com stories")

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