package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.product.ProductDeletionException;
import pl.cyfronet.s4e.ex.product.ProductException;
import pl.cyfronet.s4e.ex.product.ProductValidationException;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@BasicTest
@Slf4j
public class ProductServiceTest {
    public interface SchemaProjection {
        String getName();
    }

    public interface ProductProjection {
        SchemaProjection getSceneSchema();
        SchemaProjection getMetadataSchema();
    }

    private static final String SCHEMA_PATH_PREFIX = "classpath:schema/";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private TestResourceHelper testResourceHelper;

    private Product product108m;

    @BeforeEach
    public void beforeEach() {
        resetDb();

        product108m = productRepository.save(Product.builder()
                .name("108m")
                .displayName("108m")
                .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                .layerName("108m")
                .build());
    }

    @AfterEach
    public void afterEach() {
        resetDb();
    }

    private void resetDb() {
        testDbHelper.clean();
    }

    @Nested
    class Create {
        private ProductService.DTO.DTOBuilder dtoBuilder;

        @BeforeEach
        public void beforeEach() {
            Stream.of("Sentinel-1.scene.v1.json", "Sentinel-1.metadata.v1.json")
                    .map(name -> {
                        String content = new String(testResourceHelper.getAsBytes(SCHEMA_PATH_PREFIX + name));
                        return Schema.builder()
                                .name(name)
                                .type(name.contains("scene") ? Schema.Type.SCENE : Schema.Type.METADATA)
                                .content(content)
                                .build();
                    })
                    .forEach(schemaRepository::save);

            dtoBuilder = ProductService.DTO.builder()
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
        public void shouldCreate() throws ProductException {
            ProductService.DTO dto = dtoBuilder.build();

            assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

            Long createdId = productService.create(dto);

            val optProduct = productRepository.findByName(dto.getName(), Product.class);
            assertThat(optProduct, isPresent());

            val product = optProduct.get();
            assertThat(product.getId(), is(equalTo(createdId)));
            assertThat(product.getSceneSchema().getName(), is(equalTo(dto.getSceneSchemaName())));
            assertThat(product.getMetadataSchema().getName(), is(equalTo(dto.getMetadataSchemaName())));
        }

        @Nested
        class FieldSceneSchema {
            @Test
            public void shouldVerifyItExists() throws ProductException {
                ProductService.DTO dto = dtoBuilder
                        .sceneSchemaName("doesnt_exist")
                        .build();

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

                try {
                    productService.create(dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("sceneSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("exist"));
                }

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());
            }

            @Test
            public void shouldVerifyTypeScene() throws ProductException {
                ProductService.DTO dto = dtoBuilder
                        .sceneSchemaName("Sentinel-1.metadata.v1.json")
                        .build();

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

                try {
                    productService.create(dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("sceneSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("type"));
                }

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());
            }
        }

        @Nested
        class FieldMetadataSchema {
            @Test
            public void shouldVerifyItExists() throws ProductException {
                ProductService.DTO dto = dtoBuilder
                        .metadataSchemaName("doesnt_exist")
                        .build();

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

                try {
                    productService.create(dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("metadataSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("exist"));
                }

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());
            }

            @Test
            public void shouldVerifyTypeMetadata() throws ProductException {
                ProductService.DTO dto = dtoBuilder
                        .metadataSchemaName("Sentinel-1.scene.v1.json")
                        .build();

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

                try {
                    productService.create(dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("metadataSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("type"));
                }

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());
            }
        }

        @Nested
        class FieldGranuleArtifactRule {
            @Test
            public void shouldVerifyHasDefaultKey() throws ProductException {
                ProductService.DTO dto = dtoBuilder
                        .granuleArtifactRule(Map.of())
                        .build();

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

                try {
                    productService.create(dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("granuleArtifactRule");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("default"));
                }

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());
            }
        }
    }

    @Nested
    class Update {
        private Product product;
        private ProductService.DTO.DTOBuilder dtoBuilder;

        @BeforeEach
        public void beforeEach() {
            Map<String, Schema> schemas = Stream.of("Sentinel-1.scene.v1.json", "Sentinel-1.metadata.v1.json")
                    .map(name -> {
                        String content = new String(testResourceHelper.getAsBytes(SCHEMA_PATH_PREFIX + name));
                        return Schema.builder()
                                .name(name)
                                .type(name.contains("scene") ? Schema.Type.SCENE : Schema.Type.METADATA)
                                .content(content)
                                .build();
                    })
                    .map(schemaRepository::save)
                    .collect(Collectors.toMap(Schema::getName, schema -> schema));

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

            dtoBuilder = ProductService.DTO.builder()
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
        public void shouldThrowNFEWhenNotFound() {
            assertThat(productRepository.existsById(0L), is(false));

            assertThrows(NotFoundException.class, () ->
                    productService.update(0L, dtoBuilder.build()));
        }

        @Test
        public void shouldUpdate() throws ProductException, NotFoundException {
            val dto = dtoBuilder.build();

            productService.update(product.getId(), dto);

            Product updatedProduct = productRepository.findById(product.getId(), Product.class).get();

            assertThat(updatedProduct, allOf(
                    hasProperty("name", equalTo(dto.getName())),
                    hasProperty("displayName", equalTo(dto.getDisplayName())),
                    hasProperty("description", equalTo(dto.getDescription())),
                    hasProperty("layerName", equalTo(dto.getLayerName())),
                    hasProperty("granuleArtifactRule", hasEntry("default", "some_other_artifact"))
            ));
        }

        @Test
        public void shouldUpdateSelectedFields() throws ProductException, NotFoundException {
            val dto = ProductService.DTO.builder()
                    .name("ProductFoo")
                    .build();

            productService.update(product.getId(), dto);

            Product updatedProduct = productRepository.findById(product.getId(), Product.class).get();

            assertThat(updatedProduct, allOf(
                    hasProperty("name", not(equalTo(product.getName()))),
                    hasProperty("name", equalTo("ProductFoo")),
                    hasProperty("displayName", equalTo(product.getDisplayName())),
                    hasProperty("description", equalTo(product.getDescription())),
                    hasProperty("layerName", equalTo(product.getLayerName())),
                    hasProperty("granuleArtifactRule", equalTo(product.getGranuleArtifactRule()))
            ));
        }

        @Nested
        class FieldSceneSchema {
            @Test
            public void shouldVerifyItExists() throws ProductException, NotFoundException {
                ProductService.DTO dto = dtoBuilder
                        .sceneSchemaName("doesnt_exist")
                        .build();

                try {
                    productService.update(product.getId(), dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("sceneSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("exist"));
                }

                ProductProjection updatedProduct = productRepository.findById(product.getId(), ProductProjection.class).get();

                assertThat(updatedProduct.getSceneSchema().getName(), is(equalTo("Sentinel-1.scene.v1.json")));
            }

            @Test
            public void shouldVerifyTypeScene() throws ProductException, NotFoundException {
                ProductService.DTO dto = dtoBuilder
                        .sceneSchemaName("Sentinel-1.metadata.v1.json")
                        .build();

                try {
                    productService.update(product.getId(), dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("sceneSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("type"));
                }

                ProductProjection updatedProduct = productRepository.findById(product.getId(), ProductProjection.class).get();

                assertThat(updatedProduct.getSceneSchema().getName(), is(equalTo("Sentinel-1.scene.v1.json")));
            }
        }

        @Nested
        class FieldMetadataSchema {
            @Test
            public void shouldVerifyItExists() throws ProductException, NotFoundException {
                ProductService.DTO dto = dtoBuilder
                        .metadataSchemaName("doesnt_exist")
                        .build();

                try {
                    productService.update(product.getId(), dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("metadataSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("exist"));
                }

                ProductProjection updatedProduct = productRepository.findById(product.getId(), ProductProjection.class).get();

                assertThat(updatedProduct.getMetadataSchema().getName(), is(equalTo("Sentinel-1.metadata.v1.json")));
            }

            @Test
            public void shouldVerifyTypeMetadata() throws ProductException, NotFoundException {
                ProductService.DTO dto = dtoBuilder
                        .metadataSchemaName("Sentinel-1.scene.v1.json")
                        .build();

                try {
                    productService.update(product.getId(), dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("metadataSchemaName");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("type"));
                }

                ProductProjection updatedProduct = productRepository.findById(product.getId(), ProductProjection.class).get();

                assertThat(updatedProduct.getMetadataSchema().getName(), is(equalTo("Sentinel-1.metadata.v1.json")));
            }
        }

        @Nested
        class FieldGranuleArtifactRule {
            @Test
            public void shouldVerifyHasDefaultKey() throws ProductException, NotFoundException {
                ProductService.DTO dto = dtoBuilder
                        .granuleArtifactRule(Map.of("foo", "bar"))
                        .build();

                try {
                    productService.update(product.getId(), dto);
                    fail("Should throw");
                } catch (ProductValidationException e) {
                    BindingResult bindingResult = e.getBindingResult();
                    List<FieldError> errors = bindingResult.getFieldErrors("granuleArtifactRule");
                    assertThat(errors, hasSize(1));
                    FieldError error = errors.get(0);
                    assertThat(error.getCode(), containsString("default"));
                }

                Product updatedProduct = productRepository.findById(product.getId(), Product.class).get();

                assertThat(updatedProduct.getGranuleArtifactRule(), hasKey("default"));
            }
        }
    }

    @Nested
    class Delete {
        @Test
        public void shouldDelete() throws NotFoundException, ProductDeletionException {
            assertThat(productRepository.count(), is(equalTo(1L)));

            productService.delete(product108m.getId());

            assertThat(productRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldThrowNFEWhenNotFound() {
            assertThat(productRepository.count(), is(equalTo(1L)));
            assertThat(productRepository.existsById(0L), is(false));

            assertThrows(NotFoundException.class, () ->
                    productService.delete(0L));

            assertThat(productRepository.count(), is(equalTo(1L)));
        }

        @Nested
        class WhenProductHasScenes {
            @BeforeEach
            public void beforeEach() {
                sceneRepository.save(SceneTestHelper.sceneBuilder(product108m).build());
            }

            @Test
            public void shouldThrow() {
                assertThat(productRepository.count(), is(equalTo(1L)));

                assertThrows(ProductDeletionException.class, () ->
                        productService.delete(product108m.getId()));

                assertThat(productRepository.count(), is(equalTo(1L)));
            }
        }
    }

    @Nested
    class IsFavourite {
        @Test
        public void shouldReturnFalse() {
            assertThat(productService.isFavourite(product108m.getId()), is(false));
        }

        @Test
        public void shouldReturnFalseWhenProductDoesntExist() {
            assertThat(productRepository.existsById(0L), is(false));

            assertThat(productService.isFavourite(0L), is(false));
        }

        @Nested
        class WhenAuthenticated {
            private AppUser appUser;

            @BeforeEach
            public void beforeEach() {
                appUser = appUserRepository.save(AppUser.builder()
                        .email("get@profile.com")
                        .name("Get")
                        .surname("Profile")
                        .password("{noop}password")
                        .enabled(true)
                        .build());

                authenticateAs(appUser.getEmail());
            }

            @AfterEach
            public void afterEach() {
                SecurityContextHolder.clearContext();
            }

            @Test
            public void shouldReturnFalseByDefault() {
                assertThat(productService.isFavourite(product108m.getId()), is(false));
            }

            @Test
            public void shouldReturnFalseWhenProductDoesntExist() {
                assertThat(productRepository.existsById(0L), is(false));

                assertThat(productService.isFavourite(0L), is(false));
            }

            @Test
            public void shouldReturnTrueWhenFavourited() {
                Product product = productRepository.findByNameContainingIgnoreCase("108m").get();
                product.setFavourites(new HashSet<>(Arrays.asList(appUser)));
                productRepository.save(product);

                assertThat(productService.isFavourite(product108m.getId()), is(true));
            }
        }
    }

    private void authenticateAs(String email) {
        AppUserDetails appUserDetails = mock(AppUserDetails.class);
        when(appUserDetails.getUsername()).thenReturn(email);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getDetails()).thenReturn(appUserDetails);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }
}
