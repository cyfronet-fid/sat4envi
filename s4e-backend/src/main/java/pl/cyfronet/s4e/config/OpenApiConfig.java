/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.data.rest.customisers.QuerydslPredicateOperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@Configuration
public class OpenApiConfig {
    public static final String SECURITY_SCHEME_NAME = "bearer-token";

    @Autowired
    private QuerydslPredicateOperationCustomizer querydslPredicateOperationCustomizer;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sat4Envi backend API")
                        .description(
                                "The API documentation of the backend.\n\n" +
                                "Access to definition group `public` is unrestricted. " +
                                "To access group `provider` you need extra permissions and group `private` isn't made available publicly."
                        )
                        .version("v1")
                )
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    @Bean
    public GroupedOpenApi publicOpenApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch(prefix(
                        "/token",
                        "/products",
                        "/scenes/**",
                        "/search",
                        "/search/count"
                ))
                .addOperationCustomizer(querydslPredicateOperationCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi providerOpenApi() {
        return GroupedOpenApi.builder()
                .group("provider")
                .pathsToMatch(prefix(
                        "/token",
                        "/products",
                        "/scenes/**",
                        "/search",
                        "/search/count",
                        "/schemas/",
                        "/schemas/**",
                        "/sync-records"
                ))
                .addOperationCustomizer(querydslPredicateOperationCustomizer)
                .build();
    }

    @Bean
    public GroupedOpenApi privateOpenApi() {
        return GroupedOpenApi.builder()
                .group("private")
                .packagesToScan("pl.cyfronet.s4e")
                .addOperationCustomizer(querydslPredicateOperationCustomizer)
                .build();
    }

    private static String[] prefix(String... paths) {
        return Arrays.stream(paths)
                .map(path -> API_PREFIX_V1 + path)
                .toArray(String[]::new);
    }
}
