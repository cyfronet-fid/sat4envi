package pl.cyfronet.s4e.admin.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.*;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class AdminProductControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private TestResourceHelper testResourceHelper;

    private AppUser admin;

    private AppUser user;
    private Map<String, Schema> schemas;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        admin = appUserRepository.save(AppUser.builder()
                .email("admin@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .admin(true)
                .build());

        user = appUserRepository.save(AppUser.builder()
                .email("user@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .build());

        schemas = SchemaTestHelper.SCENE_AND_METADATA_SCHEMA_NAMES.stream()
                .map(path -> SchemaTestHelper.schemaBuilder(path, testResourceHelper).build())
                .map(schemaRepository::save)
                .collect(Collectors.toMap(Schema::getName, schema -> schema));
    }

    @Nested
    class Create {
        private AdminCreateProductRequest.AdminCreateProductRequestBuilder requestBuilder;

        @BeforeEach
        public void beforeEach() {
            requestBuilder = AdminCreateProductRequest.builder()
                    .name("Product01")
                    .displayName("Product 01")
                    .description("Product 01 __description__")
                    .legend(Legend.builder()
                            .type("Legend 01")
                            .build())
                    .layerName("product_01")
                    .sceneSchemaName("Sentinel-1.scene.v1.json")
                    .metadataSchemaName("Sentinel-1.metadata.v1.json")
                    .granuleArtifactRule(Map.of("default", "some_artifact"));
        }

        @Test
        public void shouldWork() throws Exception {
            AdminCreateProductRequest createRequest = requestBuilder.build();

            assertThat(productRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(Constants.ADMIN_PREFIX + "/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber());

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            AdminCreateProductRequest createRequest = requestBuilder.build();

            assertThat(productRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(Constants.ADMIN_PREFIX + "/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isForbidden());

            assertThat(productRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldVerify() throws Exception {
            AdminCreateProductRequest createRequest = requestBuilder
                    .layerName("product-01") // hyphen isn't allowed
                    .sceneSchemaName(null) // missing required field
                    .metadataSchemaName("non-existent.scene.json")
                    .build();

            assertThat(productRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(Constants.ADMIN_PREFIX + "/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.layerName").isArray())
                    .andExpect(jsonPath("$.sceneSchemaName").isArray())
                    // this one is verified in ProductService instead
                    .andExpect(jsonPath("$.metadataSchemaName").doesNotExist());

            assertThat(productRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldVerifyInService() throws Exception {
            AdminCreateProductRequest createRequest = requestBuilder
                    .metadataSchemaName("non-existent.scene.json")
                    .build();

            assertThat(productRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(Constants.ADMIN_PREFIX + "/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isBadRequest())
                    // this one is verified in ProductService instead
                    .andExpect(jsonPath("$.metadataSchemaName").isArray());

            assertThat(productRepository.count(), is(equalTo(0L)));
        }
    }

    @Nested
    class ReadAndList {
        private Product product;

        @BeforeEach
        public void beforeEach() {
            product = productRepository.save(Product.builder()
                    .name("Product01")
                    .displayName("Product 01")
                    .description("Product 01 __description__")
                    .legend(Legend.builder()
                            .type("Legend 01")
                            .build())
                    .layerName("product_01")
                    .sceneSchema(schemas.get("Sentinel-1.scene.v1.json"))
                    .metadataSchema(schemas.get("Sentinel-1.metadata.v1.json"))
                    .granuleArtifactRule(Map.of("default", "some_artifact"))
                    .build());
        }

        @Test
        public void shouldList() throws Exception {
            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/products")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].name", is(equalTo("Product01"))))
                    .andExpect(jsonPath("$[0].displayName", is(equalTo("Product 01"))))
                    .andExpect(jsonPath("$[0].description", is(equalTo("Product 01 __description__"))))
                    .andExpect(jsonPath("$[0].legend.type", is(equalTo("Legend 01"))))
                    .andExpect(jsonPath("$[0].layerName", is(equalTo("product_01"))))
                    .andExpect(jsonPath("$[0].granuleArtifactRule.default", is(equalTo("some_artifact"))));

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldRead() throws Exception {
            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name", is(equalTo("Product01"))))
                    .andExpect(jsonPath("$.displayName", is(equalTo("Product 01"))))
                    .andExpect(jsonPath("$.description", is(equalTo("Product 01 __description__"))))
                    .andExpect(jsonPath("$.legend.type", is(equalTo("Legend 01"))))
                    .andExpect(jsonPath("$.layerName", is(equalTo("product_01"))))
                    .andExpect(jsonPath("$.granuleArtifactRule.default", is(equalTo("some_artifact"))));

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldHandleNotFound() throws Exception {
            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(get(Constants.ADMIN_PREFIX + "/products/{id}", product.getId() + 1)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(productRepository.count(), is(equalTo(1L)));
        }
    }

    @Nested
    class Update {
        private AdminUpdateProductRequest.AdminUpdateProductRequestBuilder requestBuilder;

        private Product product;

        @BeforeEach
        public void beforeEach() {
            product = productRepository.save(Product.builder()
                    .name("Product01")
                    .displayName("Product 01")
                    .description("Product 01 __description__")
                    .legend(Legend.builder()
                            .type("Legend 01")
                            .build())
                    .layerName("product_01")
                    .sceneSchema(schemas.get("Sentinel-1.scene.v1.json"))
                    .metadataSchema(schemas.get("Sentinel-1.metadata.v1.json"))
                    .granuleArtifactRule(Map.of("default", "some_artifact"))
                    .build());

            requestBuilder = AdminUpdateProductRequest.builder()
                    .name("Product02")
                    .displayName("Product 02")
                    .description("Product 02 __description__")
                    .legend(Legend.builder()
                            .type("Legend 02")
                            .build())
                    .layerName("product_02")
                    .granuleArtifactRule(Map.of("default", "some_other_artifact"));
        }

        @Test
        public void shouldWork() throws Exception {
            AdminUpdateProductRequest updateRequest = requestBuilder.build();

            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(patch(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name", is(equalTo("Product02"))))
                    .andExpect(jsonPath("$.displayName", is(equalTo("Product 02"))))
                    .andExpect(jsonPath("$.description", is(equalTo("Product 02 __description__"))))
                    .andExpect(jsonPath("$.legend.type", is(equalTo("Legend 02"))))
                    .andExpect(jsonPath("$.layerName", is(equalTo("product_02"))))
                    .andExpect(jsonPath("$.granuleArtifactRule.default", is(equalTo("some_other_artifact"))));

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldUpdateSelectedFields() throws Exception {
            AdminUpdateProductRequest updateRequest = AdminUpdateProductRequest.builder()
                    .name("Product02")
                    .granuleArtifactRule(Map.of("default", "some_other_artifact"))
                    .build();

            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(patch(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name", is(equalTo("Product02"))))
                    .andExpect(jsonPath("$.displayName", is(equalTo("Product 01"))))
                    .andExpect(jsonPath("$.description", is(equalTo("Product 01 __description__"))))
                    .andExpect(jsonPath("$.legend.type", is(equalTo("Legend 01"))))
                    .andExpect(jsonPath("$.layerName", is(equalTo("product_01"))))
                    .andExpect(jsonPath("$.granuleArtifactRule.default", is(equalTo("some_other_artifact"))));

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            AdminUpdateProductRequest updateRequest = requestBuilder.build();

            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(patch(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user, objectMapper))
                    .content(objectMapper.writeValueAsBytes(updateRequest)))
                    .andExpect(status().isForbidden());

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldVerify() throws Exception {
            AdminUpdateProductRequest createRequest = AdminUpdateProductRequest.builder()
                    .layerName("product-01") // hyphen isn't allowed
                    .metadataSchemaName("non-existent.scene.json")
                    .build();

            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(patch(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.layerName").isArray())
                    // this one is verified in ProductService instead
                    .andExpect(jsonPath("$.metadataSchemaName").doesNotExist());

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldVerifyInService() throws Exception {
            AdminUpdateProductRequest createRequest = AdminUpdateProductRequest.builder()
                    .metadataSchemaName("non-existent.scene.json")
                    .build();

            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(patch(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isBadRequest())
                    // this one is verified in ProductService instead
                    .andExpect(jsonPath("$.metadataSchemaName").isArray());

            assertThat(productRepository.count(), is(equalTo(1L)));
        }
    }

    @Nested
    class Delete {
        private Product product;

        @BeforeEach
        public void beforeEach() {
            product = productRepository.save(Product.builder()
                    .name("Product01")
                    .displayName("Product 01")
                    .description("Product 01 __description__")
                    .legend(Legend.builder()
                            .type("Legend 01")
                            .build())
                    .layerName("product_01")
                    .sceneSchema(schemas.get("Sentinel-1.scene.v1.json"))
                    .metadataSchema(schemas.get("Sentinel-1.metadata.v1.json"))
                    .granuleArtifactRule(Map.of("default", "default_artifact"))
                    .build());
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(productRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldHandleNotFound() throws Exception {
            assertThat(productRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}", product.getId() + 1)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Nested
        class WhenProductHasScenes {
            @BeforeEach
            public void beforeEach() {
                sceneRepository.save(SceneTestHelper.sceneBuilder(product).build());
            }

            @Test
            public void shouldReturnBadRequest() throws Exception {
                assertThat(productRepository.count(), is(equalTo(1L)));

                mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.__general__", containsStringIgnoringCase("scene")));

                assertThat(productRepository.count(), is(equalTo(1L)));
            }
        }

        @Nested
        class WhenProductIsFavourited {
            @BeforeEach
            public void beforeEach() {
                product.addFavourite(user);
                productRepository.save(product);
            }

            @Test
            public void shouldWork() throws Exception {
                assertThat(productRepository.count(), is(equalTo(1L)));
                assertThat(productRepository.isFavouriteByEmailAndProductId(user.getEmail(), product.getId()), is(true));

                mockMvc.perform(delete(Constants.ADMIN_PREFIX + "/products/{id}", product.getId())
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertThat(productRepository.count(), is(equalTo(0L)));
            }
        }
    }
}
