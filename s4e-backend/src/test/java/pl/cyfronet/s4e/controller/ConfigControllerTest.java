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

package pl.cyfronet.s4e.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.properties.OsmProperties;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;

@AutoConfigureMockMvc
@BasicTest
public class ConfigControllerTest {
    @Autowired
    private GeoServerProperties geoServerProperties;

    @Autowired
    private OsmProperties osmProperties;

    @Value("${recaptcha.validation.siteKey}")
    private String recaptchaSiteKey;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnConfiguration() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.osmUrl").value(osmProperties.getUrl()))
                .andExpect(jsonPath("$.geoserverUrl").value(geoServerProperties.getOutsideBaseUrl()))
                .andExpect(jsonPath("$.recaptchaSiteKey").value(recaptchaSiteKey));
    }

    @Nested
    class Search {
        private static final String SCENE_SCHEMA_PATH = "classpath:schema/Sentinel-1.scene.v1.json";

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private SchemaRepository schemaRepository;

        @Autowired
        private TestDbHelper testDbHelper;

        @Autowired
        private TestResourceHelper testResourceHelper;

        @BeforeEach
        public void beforeEach() {
            testDbHelper.clean();

            String content = new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH));
            Schema testSchema = schemaRepository.save(Schema.builder()
                    .name("test.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .build());
            Schema sentinel1Schema = schemaRepository.save(Schema.builder()
                    .name("Sentinel-1.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .build());

            productRepository.save(productBuilder()
                    .metadataSchema(testSchema)
                    .build());
            productRepository.save(productBuilder()
                    .metadataSchema(sentinel1Schema)
                    .build());
        }

        @Test
        public void shouldReturn() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/config/sentinel-search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sections[0].name", is(equalTo("sentinel-1"))))
                    .andExpect(jsonPath("$.sections[1].name", is(equalTo("test.metadata.v1.json"))));
        }
    }

}
