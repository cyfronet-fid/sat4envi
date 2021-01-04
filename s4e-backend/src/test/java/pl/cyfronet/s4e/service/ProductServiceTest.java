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

package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import pl.cyfronet.s4e.*;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.product.ProductDeletionException;
import pl.cyfronet.s4e.ex.product.ProductException;
import pl.cyfronet.s4e.ex.product.ProductValidationException;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_READ_AUTHORITY_PREFIX;

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

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private LicenseGrantRepository licenseGrantRepository;

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
                .granuleArtifactRule(Map.of("default", "default_artifact"))
                .authorizedOnly(false)
                .accessType(Product.AccessType.OPEN)
                .rank(1000L)
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
    class FindAllAuthorizedFetchProductCategory {
        private Product productPrivate;

        @BeforeEach
        public void beforeEach() {
            productPrivate = productRepository.save(SceneTestHelper.productBuilder()
                    .accessType(Product.AccessType.PRIVATE)
                    .build());

            // Another private Product, make sure that many private Products are handled correctly.
            productRepository.save(SceneTestHelper.productBuilder()
                    .accessType(Product.AccessType.PRIVATE)
                    .build());
        }

        @Test
        public void shouldFilterForUserWithoutLicense() {
            val userDetails = mock(AppUserDetails.class);
            when(userDetails.getAuthorities()).thenReturn(Set.of());

            val results = productService.findAllAuthorizedFetchProductCategory(userDetails, ProjectionWithId.class);

            assertThat(results, hasSize(1));
            assertThat(results.get(0).getId(), is(equalTo(product108m.getId())));
        }

        @Test
        public void shouldFilterForAnonymousUser() {
            val results = productService.findAllAuthorizedFetchProductCategory(null, ProjectionWithId.class);

            assertThat(results, hasSize(1));
            assertThat(results.get(0).getId(), is(equalTo(product108m.getId())));
        }

        @Test
        public void shouldReturnForLicensedUser() {
            val userDetails = mock(AppUserDetails.class);
            when(userDetails.getAuthorities()).thenReturn(
                    Set.of(new SimpleGrantedAuthority(LICENSE_READ_AUTHORITY_PREFIX + productPrivate.getId()))
            );

            val results = productService.findAllAuthorizedFetchProductCategory(userDetails, ProjectionWithId.class);

            assertThat(results, containsInAnyOrder(
                    hasProperty("id", is(equalTo(product108m.getId()))),
                    hasProperty("id", is(equalTo(productPrivate.getId())))
            ));
        }
    }

    @Nested
    class Create {
        private ProductService.DTO.DTOBuilder dtoBuilder;

        @BeforeEach
        public void beforeEach() {
            SchemaTestHelper.SCENE_AND_METADATA_SCHEMA_NAMES.stream()
                    .map(path -> SchemaTestHelper.schemaBuilder(path, testResourceHelper).build())
                    .forEach(schemaRepository::save);

            dtoBuilder = ProductService.DTO.builder()
                    .name("Product01")
                    .displayName("Product 01")
                    .description("Product 01 __description__")
                    .authorizedOnly(false)
                    .accessType(Product.AccessType.OPEN)
                    .legend(Legend.builder()
                            .type("Legend 01")
                            .build())
                    .layerName("product_01")
                    .sceneSchemaName("Sentinel-1.scene.v1.json")
                    .metadataSchemaName("Sentinel-1.metadata.v1.json")
                    .granuleArtifactRule(Map.of("default", "some_artifact"))
                    .rank(2000L);
        }

        @Test
        public void shouldCreate() throws ProductException, NotFoundException {
            ProductService.DTO dto = dtoBuilder.build();

            assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

            Long createdId = productService.create(dto);

            val optProduct = productRepository.findByName(dto.getName(), Product.class);
            assertThat(optProduct, isPresent());

            val product = optProduct.get();
            assertThat(product.getId(), is(equalTo(createdId)));
            assertThat(product.getSceneSchema().getName(), is(equalTo(dto.getSceneSchemaName())));
            assertThat(product.getMetadataSchema().getName(), is(equalTo(dto.getMetadataSchemaName())));

            val categoryName = product.getProductCategory().getName();
            assertThat(categoryName, is(equalTo("other")));
        }

        @Nested
        class FieldSceneSchema {
            @Test
            public void shouldVerifyItExists() throws ProductException, NotFoundException {
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
            public void shouldVerifyTypeScene() throws ProductException, NotFoundException {
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
            public void shouldVerifyItExists() throws ProductException, NotFoundException {
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
            public void shouldVerifyTypeMetadata() throws ProductException, NotFoundException {
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
            public void shouldVerifyHasDefaultKey() throws ProductException, NotFoundException {
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

        @Nested
        class ProductCategory {
            @Test
            public void shouldVerifyItExists() throws ProductException {
                ProductService.DTO dto = dtoBuilder
                        .productCategoryName("doesnt_exist")
                        .build();
                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

                try {
                    productService.create(dto);
                    fail("Should throw");
                } catch (NotFoundException e) {
                    assertThat(e.getMessage(), containsString(dto.getProductCategoryName()));
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
            Map<String, Schema> schemas = SchemaTestHelper.SCENE_AND_METADATA_SCHEMA_NAMES.stream()
                    .map(path -> SchemaTestHelper.schemaBuilder(path, testResourceHelper).build())
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
                    .authorizedOnly(false)
                    .accessType(Product.AccessType.OPEN)
                    .rank(2000L)
                    .build());

            dtoBuilder = ProductService.DTO.builder()
                    .name("Product02")
                    .displayName("Product 02")
                    .description("Product 02 __description__")
                    .legend(Legend.builder()
                            .type("Legend 02")
                            .build())
                    .layerName("product_02")
                    .granuleArtifactRule(Map.of("default", "some_other_artifact"))
                    .rank(3000L);
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

            Product updatedProduct = productRepository.findByIdFetchCategory(product.getId(), Product.class).get();

            assertThat(updatedProduct, allOf(
                    hasProperty("name", equalTo(dto.getName())),
                    hasProperty("displayName", equalTo(dto.getDisplayName())),
                    hasProperty("description", equalTo(dto.getDescription())),
                    hasProperty("layerName", equalTo(dto.getLayerName())),
                    hasProperty("granuleArtifactRule", hasEntry("default", "some_other_artifact")),
                    hasProperty("rank", equalTo(3000L))
            ));

            val categoryName = updatedProduct.getProductCategory().getName();
            assertThat(categoryName, is(equalTo("other")));
        }

        @Test
        public void shouldUpdateSelectedFields() throws ProductException, NotFoundException {
            val dto = ProductService.DTO.builder()
                    .name("ProductFoo")
                    .rank(null)
                    .build();

            productService.update(product.getId(), dto);

            Product updatedProduct = productRepository.findById(product.getId(), Product.class).get();

            assertThat(updatedProduct, allOf(
                    hasProperty("name", not(equalTo(product.getName()))),
                    hasProperty("name", equalTo("ProductFoo")),
                    hasProperty("displayName", equalTo(product.getDisplayName())),
                    hasProperty("description", equalTo(product.getDescription())),
                    hasProperty("layerName", equalTo(product.getLayerName())),
                    hasProperty("granuleArtifactRule", equalTo(product.getGranuleArtifactRule())),
                    hasProperty("rank", equalTo(2000L))
            ));
        }

        @Nested
        class FieldAccessType {
            @BeforeEach
            public void beforeEach() {
                product.setAccessType(Product.AccessType.PRIVATE);
                product = productRepository.save(product);

                val institution = institutionRepository.save(Institution.builder()
                        .name("test")
                        .slug("test")
                        .build());

                licenseGrantRepository.save(LicenseGrant.builder()
                        .institution(institution)
                        .product(product)
                        .build());
            }

            @ParameterizedTest
            @CsvSource({
                    "OPEN,0",
                    "EUMETSAT,0",
                    "PRIVATE,1"
            })
            public void test(Product.AccessType targetAccessType, Long expectedLicenseGrantCount) throws NotFoundException, ProductException {
                val dto = ProductService.DTO.builder()
                        .accessType(targetAccessType)
                        .build();

                assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

                productService.update(product.getId(), dto);

                assertThat(licenseGrantRepository.count(), is(equalTo(expectedLicenseGrantCount)));

                Product updatedProduct = productRepository.findById(product.getId(), Product.class).get();
                assertThat(updatedProduct, hasProperty("accessType", equalTo(targetAccessType)));
            }
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

        @Nested
        class FieldProductCategory {
            @Test
            public void shouldVerifyItExists() throws ProductException {
                ProductService.DTO dto = dtoBuilder
                        .productCategoryName("doesnt_exist")
                        .build();
                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());

                try {
                    productService.create(dto);
                    fail("Should throw");
                } catch (NotFoundException e) {
                    assertThat(e.getMessage(), containsString(dto.getProductCategoryName()));
                }

                assertThat(productRepository.findByName(dto.getName(), Product.class), isEmpty());
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
                product.setFavourites(Set.of(appUser));
                productRepository.save(product);

                assertThat(productService.isFavourite(product108m.getId()), is(true));
            }
        }
    }

    private static void authenticateAs(String email) {
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
