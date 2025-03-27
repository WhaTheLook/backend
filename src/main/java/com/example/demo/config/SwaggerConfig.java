package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jdk.javadoc.doclet.Doclet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Parameter;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI getOpenApi() {

        String jwt = "Authorization";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        Components components = new Components()
                .addSecuritySchemes(jwt, new SecurityScheme()
                        .name(jwt).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));


        return new OpenAPI()
                .components(new Components())
                .info(getInfo())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info getInfo() {
        return new Info()
                .version("1.0.0")   // 문서 버전
                .description("COMMERCE REST API DOC")   // 문서 설명
                .title("COMMERCE"); // 문서 제목
    }
}