/*
 * Copyright 2020 ACC Cyfronet AGH
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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class SchemaControllerTest {
    private static final String SCENE_SCHEMA_PATH = "classpath:schema/Sentinel-1.scene.v1.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private TestResourceHelper testResourceHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        createSchemas();
    }

    @Autowired
    private SchemaRepository schemaRepository;

    private void createSchemas() {
        String content = new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH));
        Schema testSceneV1 = schemaRepository.save(Schema.builder()
                .name("test.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(content)
                .build());
        schemaRepository.save(Schema.builder()
                .name("test.scene.v2.json")
                .type(Schema.Type.SCENE)
                .content(content)
                .previous(testSceneV1)
                .build());
        schemaRepository.save(Schema.builder()
                .name("abc.metadata.v1.json")
                .type(Schema.Type.METADATA)
                .content(content)
                .build());
    }

    @Test
    public void shouldListSchemas() throws Exception {
        assertThat(schemaRepository.count(), is(equalTo(3L)));

        mockMvc.perform(get(API_PREFIX_V1 + "/schemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(equalTo(3))))
                .andExpect(jsonPath("$[0].name", is(equalTo("abc.metadata.v1.json"))))
                .andExpect(jsonPath("$[2].previous.name", is(equalTo("test.scene.v1.json"))));
    }

    @Test
    public void shouldReturnSchema() throws Exception {
        assertThat(schemaRepository.count(), is(equalTo(3L)));

        mockMvc.perform(get(API_PREFIX_V1 + "/schemas/{name}", "test.scene.v1.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH))));
    }
}
