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
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.Property;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.PropertyRepository;
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
    private static final String CONFIG_URL = API_PREFIX_V1 + "/config";

    @Autowired
    private GeoServerProperties geoServerProperties;

    @Autowired
    private OsmProperties osmProperties;

    @Value("${recaptcha.validation.siteKey}")
    private String recaptchaSiteKey;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void shouldReturnConfiguration() throws Exception {
        mockMvc.perform(get(CONFIG_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.osmUrl").value(osmProperties.getUrl()))
                .andExpect(jsonPath("$.geoserverUrl").value(geoServerProperties.getOutsideBaseUrl()))
                .andExpect(jsonPath("$.recaptchaSiteKey").value(recaptchaSiteKey));
    }

    @Nested
    class Helpdesk {
        @Autowired
        private PropertyRepository propertyRepository;

        @Test
        public void shouldWork() throws Exception {
            setHelpdeskProperty("foo=bar,baz=foo=bar,bar=,foobar,");

            mockMvc.perform(get(CONFIG_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.helpdesk.foo").value("bar"))
                    .andExpect(jsonPath("$.helpdesk.baz").value("foo=bar"))
                    .andExpect(jsonPath("$.helpdesk.bar").value(""))
                    .andExpect(jsonPath("$.helpdesk.foobar").doesNotExist());
        }

        @Test
        public void shouldTakeFirstKeyIfThereAreDuplicates() throws Exception {
            setHelpdeskProperty("foo=bar,foo=baz");

            mockMvc.perform(get(CONFIG_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.helpdesk.foo").value("bar"));
        }

        @Test
        public void shouldBeEmptyIfEmptyProperty() throws Exception {
            setHelpdeskProperty("");

            mockMvc.perform(get(CONFIG_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.helpdesk").isEmpty());
        }

        @Test
        public void shouldBeEmptyIfPropertyUnset() throws Exception {
            mockMvc.perform(get(CONFIG_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.helpdesk").isEmpty());
        }

        private void setHelpdeskProperty(String value) {
            propertyRepository.save(Property.builder()
                    .name(Constants.PROPERTY_HELPDESK_CONFIG)
                    .value(value)
                    .build());
        }
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
            Schema testSchema1 = schemaRepository.save(Schema.builder()
                    .name("test-1.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .build());
            Schema testSchema2 = schemaRepository.save(Schema.builder()
                    .name("test-2.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .build());
            Schema sentinel1Schema = schemaRepository.save(Schema.builder()
                    .name("Sentinel-1.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .build());
            Schema sentinel2Schema = schemaRepository.save(Schema.builder()
                    .name("Sentinel-2.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .build());
            Schema msgSchema = schemaRepository.save(Schema.builder()
                    .name("MSG.metadata.v1.json")
                    .type(Schema.Type.METADATA)
                    .content(content)
                    .build());

            // The products are deliberately created in this order, to verify extra schemas are sorted by name.
            productRepository.save(productBuilder()
                    .name("test-2")
                    .displayName("test 2")
                    .metadataSchema(testSchema2)
                    .build());
            productRepository.save(productBuilder()
                    .name("test-1")
                    .displayName("test 1")
                    .metadataSchema(testSchema1)
                    .build());
            productRepository.save(productBuilder()
                    .metadataSchema(msgSchema)
                    .build());
            productRepository.save(productBuilder()
                    .metadataSchema(sentinel2Schema)
                    .build());
            productRepository.save(productBuilder()
                    .metadataSchema(sentinel1Schema)
                    .build());
        }

        @Test
        public void shouldReturn() throws Exception {
            mockMvc.perform(get(API_PREFIX_V1 + "/config/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.common.params", hasSize(4)))
                    .andExpect(jsonPath("$.common.params[0].queryParam", is(equalTo("sortBy"))))
                    .andExpect(jsonPath("$.common.params[0].label", is(equalTo("Sortuj po"))))
                    .andExpect(jsonPath("$.common.params[0].values", hasSize(3)))
                    .andExpect(jsonPath("$.common.params[0].values[0].value", is(equalTo("sensingTime"))))
                    .andExpect(jsonPath("$.common.params[0].values[0].label", is(equalTo("Czas detekcji"))))
                    .andExpect(jsonPath("$.common.params[1].queryParam", is(equalTo("order"))))
                    .andExpect(jsonPath("$.common.params[1].label", is(equalTo("Kolejność"))))
                    .andExpect(jsonPath("$.sections", hasSize(5)))
                    .andExpect(jsonPath("$.sections[0].label", is(equalTo("Sentinel-1"))))
                    .andExpect(jsonPath("$.sections[0].name", is(equalTo("Sentinel-1.metadata.v1.json"))))
                    .andExpect(jsonPath("$.sections[1].label", is(equalTo("Sentinel-2"))))
                    .andExpect(jsonPath("$.sections[2].label", is(equalTo("MSG"))))
                    .andExpect(jsonPath("$.sections[3].label", is(equalTo("test-1 v1"))))
                    .andExpect(jsonPath("$.sections[3].name", is(equalTo("test-1.metadata.v1.json"))))
                    .andExpect(jsonPath("$.sections[4].label", is(equalTo("test-2 v1"))))
                    .andExpect(jsonPath("$.sections[3].params[0].queryParam", is(equalTo("productType"))))
                    .andExpect(jsonPath("$.sections[3].params[0].label", is(equalTo("Produkt"))))
                    .andExpect(jsonPath("$.sections[3].params[0].values", hasSize(1)))
                    .andExpect(jsonPath("$.sections[3].params[0].values[0].value", is(equalTo("test-1"))))
                    .andExpect(jsonPath("$.sections[3].params[0].values[0].label", is(equalTo("test 1"))));

        }
    }

}
