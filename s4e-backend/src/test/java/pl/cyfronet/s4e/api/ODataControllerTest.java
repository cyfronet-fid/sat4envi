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

package pl.cyfronet.s4e.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class ODataControllerTest {
    public static final String PROFILE_EMAIL = "get@profile.com";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private MockMvc mockMvc;

    private AppUser appUser;
    private Scene scene;

    @BeforeEach
    public void beforeEach() {
        reset(s3Presigner);
        testDbHelper.clean();
        //add product
        Product product = productRepository.save(productBuilder().build());
        //addscenewithmetadata
        scene = sceneRepository.save(buildScene(product));
        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    private Scene buildScene(Product product) {
        JsonNode metadataContent = SceneTestHelper.getMetadataContentWithNumber(0);
        Scene scene = SceneTestHelper.sceneWithMetadataBuilder(product, metadataContent).build();
        scene.setSceneContent(SceneTestHelper.getSceneContent());
        return scene;
    }

    @Nested
    class Download {
        @Nested
        class DefaultZip {
            private static final String DEFAULT_DOWNLOAD_URL_TEMPLATE =
                    API_PREFIX_V1 + "/dhus/odata/v1/Products('{sceneId}')/$value";

            @Test
            public void shouldRedirectToDownloadLink() throws Exception {
                String redirectUrl = "https://domain.pl/test?sth=value";

                PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
                when(pgor.isBrowserExecutable()).thenReturn(true);
                when(pgor.url()).thenReturn(new URL(redirectUrl));
                when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

                mockMvc.perform(get(DEFAULT_DOWNLOAD_URL_TEMPLATE, scene.getId())
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isSeeOther())
                        .andExpect(redirectedUrl(redirectUrl));
            }

            @Test
            public void shouldReturn401IfUnauthenticated() throws Exception {
                mockMvc.perform(get(DEFAULT_DOWNLOAD_URL_TEMPLATE, scene.getId()))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            public void shouldReturn404IfSceneNotFound() throws Exception {
                mockMvc.perform(get(DEFAULT_DOWNLOAD_URL_TEMPLATE, -1L)
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isNotFound());
            }
        }

        @Nested
        class Specific {
            private static final String SPECIFIC_DOWNLOAD_URL_TEMPLATE =
                    API_PREFIX_V1 + "/dhus/odata/v1/Products('{sceneId}')/Nodes('ignore')/Nodes('{type}')/$value";

            @Test
            public void shouldRedirectToDownloadLink() throws Exception {
                String redirectUrl = "https://domain.pl/test?sth=value";

                PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
                when(pgor.isBrowserExecutable()).thenReturn(true);
                when(pgor.url()).thenReturn(new URL(redirectUrl));
                when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

                mockMvc.perform(get(SPECIFIC_DOWNLOAD_URL_TEMPLATE, scene.getId(), "manifest")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isSeeOther())
                        .andExpect(redirectedUrl(redirectUrl));
            }

            @Test
            public void shouldReturn404IfArtifactNotFound() throws Exception {
                mockMvc.perform(get(SPECIFIC_DOWNLOAD_URL_TEMPLATE, scene.getId(), "notFound")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isNotFound());
            }

            @Test
            public void shouldReturn401IfUnauthenticated() throws Exception {
                mockMvc.perform(get(SPECIFIC_DOWNLOAD_URL_TEMPLATE, scene.getId(), "manifest"))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            public void shouldReturn404IfSceneNotFound() throws Exception {
                mockMvc.perform(get(SPECIFIC_DOWNLOAD_URL_TEMPLATE, -1L, "manifest")
                        .with(jwtBearerToken(appUser, objectMapper)))
                        .andExpect(status().isNotFound());
            }

        }
    }
}
