package com.vodokanal.accounting.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Accounting Service API")
                        .version("1.0.0")
                        .description("REST API Сервиса учета АО «Водоканал»")
                        .contact(new Contact()
                                .name("GitHub Repository")
                                .url("https://github.com")));
    }
}
