package pl.cyfronet.s4e.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    public void setUp() throws Exception {
        reset(s3Presigner);
        testDbHelper.clean();
        //add product
        Product product = productRepository.save(productBuilder().build());
        //addscenewithmetadata
        List<Scene> scenes = new ArrayList<>();
        for (long j = 0; j < 30; j++) {
            scenes.add(buildScene(product, j));
        }
        sceneRepository.saveAll(scenes);
        appUser = appUserRepository.save(AppUser.builder()
                .email(PROFILE_EMAIL)
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    @AfterEach
    public void tearDown() {
        testDbHelper.clean();
    }

    private Scene buildScene(Product product, long number) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(SceneTestHelper.getMetaDataWithNumber(number));
        return SceneTestHelper.sceneWithMetadataBuilder(product, jsonNode)
                .build();
    }

    @Test
    public void shouldRedirectToDownloadLink() throws Exception {
        // TODO fix
    }

    @Test
    public void shouldReturn404IfSceneNotFound() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/dhus/odata/v1/Products('{id}')/$value", -1L)
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isNotFound());
    }
}
