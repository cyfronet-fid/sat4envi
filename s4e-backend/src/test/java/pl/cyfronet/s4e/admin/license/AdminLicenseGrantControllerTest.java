package pl.cyfronet.s4e.admin.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.LicenseGrant;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.LicenseGrantRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class AdminLicenseGrantControllerTest {
    private static final String URL_PREFIX = Constants.ADMIN_PREFIX + "/license-grants";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private LicenseGrantRepository licenseGrantRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private MockMvc mockMvc;

    private AppUser admin;

    private AppUser user;
    private Product product;
    private Institution institution;

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

        product = productRepository.save(SceneTestHelper.productBuilder()
                .accessType(Product.AccessType.PRIVATE)
                .build());

        institution = institutionRepository.save(Institution.builder()
                .name("A")
                .slug("A")
                .build());
    }

    @Nested
    class Create {
        private AdminCreateLicenseGrantRequest.AdminCreateLicenseGrantRequestBuilder requestBuilder;

        @BeforeEach
        public void beforeEach() {
            requestBuilder = AdminCreateLicenseGrantRequest.builder()
                .institutionSlug(institution.getSlug())
                .productId(product.getId());
        }

        @ParameterizedTest
        @ValueSource(booleans = { true, false })
        public void shouldWork(boolean owner) throws Exception {
            val createRequest = requestBuilder
                    .owner(owner)
                    .build();

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.productId", is(equalTo(product.getId().intValue()))))
                    .andExpect(jsonPath("$.institutionSlug", is(equalTo(institution.getSlug()))))
                    .andExpect(jsonPath("$.owner", is(owner)));

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            transactionTemplate.executeWithoutResult((transactionStatus) -> {
                val newLicenseGrant = licenseGrantRepository.findAll().get(0);
                assertThat(newLicenseGrant, allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("product", equalTo(product)),
                        hasProperty("institution", equalTo(institution)),
                        hasProperty("owner", equalTo(owner))
                ));
            });
        }

        @Test
        public void shouldBeSecured() throws Exception {
            val createRequest = requestBuilder.build();

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isForbidden());

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldVerifyProductExists() throws Exception {
            val createRequest = requestBuilder
                    .productId(product.getId() + 1)
                    .build();

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.productId", hasSize(1)));

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));
        }

        @ParameterizedTest
        @ValueSource(strings = { "OPEN", "EUMETSAT" })
        public void shouldVerifyProductHasPrivateAccessType(String accessType) throws Exception {
            product.setAccessType(Product.AccessType.valueOf(accessType));
            product = productRepository.save(product);

            val createRequest = requestBuilder.build();

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.productId[0]", containsString("PRIVATE")));

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldVerifyInstitutionExists() throws Exception {
            val createRequest = requestBuilder
                    .institutionSlug("B")
                    .build();

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));

            mockMvc.perform(post(URL_PREFIX)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(createRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.institutionSlug", hasSize(1)));

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));
        }
    }

    @Nested
    class ReadAndList {
        private LicenseGrant licenseGrant;

        @BeforeEach
        public void beforeEach() {
            licenseGrant = licenseGrantRepository.save(LicenseGrant.builder()
                    .product(product)
                    .institution(institution)
                    .build());
        }

        @Test
        public void shouldList() throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(get(URL_PREFIX)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldRead() throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(get(URL_PREFIX + "/{id}", licenseGrant.getId())
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.productId", is(equalTo(product.getId().intValue()))))
                    .andExpect(jsonPath("$.institutionSlug", is(equalTo(institution.getSlug()))))
                    .andExpect(jsonPath("$.owner", is(equalTo(false))));

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }

        @Test
        public void shouldHandleNotFound() throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(get(URL_PREFIX + "/{id}", licenseGrant.getId() + 1)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }
    }

    @Nested
    class Update {
        private AdminUpdateLicenseGrantRequest.AdminUpdateLicenseGrantRequestBuilder requestBuilder;
        private LicenseGrant licenseGrant;

        @BeforeEach
        public void beforeEach() {
            licenseGrant = licenseGrantRepository.save(LicenseGrant.builder()
                    .product(product)
                    .institution(institution)
                    .build());

            requestBuilder = AdminUpdateLicenseGrantRequest.builder();
        }

        @ParameterizedTest
        @ValueSource(booleans = { true, false })
        public void shouldWork(boolean owner) throws Exception {
            val updateRequest = requestBuilder.owner(owner).build();

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(patch(URL_PREFIX + "/{id}", licenseGrant.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(admin, objectMapper))
                    .content(objectMapper.writeValueAsBytes(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.productId", is(equalTo(product.getId().intValue()))))
                    .andExpect(jsonPath("$.institutionSlug", is(equalTo(institution.getSlug()))))
                    .andExpect(jsonPath("$.owner", is(owner)));

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            transactionTemplate.executeWithoutResult((transactionStatus) -> {
                val updatedLicenseGrant = licenseGrantRepository.findAll().get(0);
                assertThat(updatedLicenseGrant, allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("product", equalTo(product)),
                        hasProperty("institution", equalTo(institution)),
                        hasProperty("owner", equalTo(owner))
                ));
            });
        }

        @Test
        public void shouldBeSecured() throws Exception {
            val updateRequest = requestBuilder.build();

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(patch(URL_PREFIX + "/{id}", licenseGrant.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(user, objectMapper))
                    .content(objectMapper.writeValueAsBytes(updateRequest)))
                    .andExpect(status().isForbidden());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }
    }

    @Nested
    class Delete {
        private LicenseGrant licenseGrant;

        @BeforeEach
        public void beforeEach() {
            licenseGrant = licenseGrantRepository.save(LicenseGrant.builder()
                    .product(product)
                    .institution(institution)
                    .build());
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(URL_PREFIX + "/{id}", licenseGrant.getId())
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNoContent());

            assertThat(licenseGrantRepository.count(), is(equalTo(0L)));
        }

        @Test
        public void shouldHandleNotFound() throws Exception {
            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));

            mockMvc.perform(delete(URL_PREFIX + "/{id}", licenseGrant.getId() + 1)
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isNotFound());

            assertThat(licenseGrantRepository.count(), is(equalTo(1L)));
        }
    }
}
