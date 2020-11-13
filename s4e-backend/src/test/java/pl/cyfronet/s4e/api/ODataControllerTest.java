package pl.cyfronet.s4e.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
        scene = sceneRepository.save(buildScene(product, 0));
        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    @SneakyThrows
    private Scene buildScene(Product product, long number) {
        JsonNode jsonNode = objectMapper.readTree(SceneTestHelper.getMetaDataWithNumber(number));
        Scene scene = SceneTestHelper.sceneWithMetadataBuilder(product, jsonNode)
                .build();
        scene.setSceneContent(objectMapper.readTree(SceneTestHelper.getSceneContent()));
        return scene;
    }

    @Nested
    class Download {
        @Nested
        class Default {
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
