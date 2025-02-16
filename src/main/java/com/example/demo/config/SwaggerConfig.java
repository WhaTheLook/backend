package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI getOpenApi() {
        return new OpenAPI().components(new Components())
                .info(getInfo());
    }

    private Info getInfo() {
        return new Info()
                .version("1.0.0")   // 문서 버전
                .description("COMMERCE REST API DOC")   // 문서 설명
                .title("COMMERCE"); // 문서 제목
    }
}