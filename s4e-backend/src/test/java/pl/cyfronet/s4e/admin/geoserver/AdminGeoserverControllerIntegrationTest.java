package pl.cyfronet.s4e.admin.geoserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.WMSOverlayRepository;
import pl.cyfronet.s4e.geoserver.op.GeoServerOperations;
import pl.cyfronet.s4e.geoserver.op.SeedProductsTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@IntegrationTest
@AutoConfigureMockMvc
class AdminGeoserverControllerIntegrationTest {
    private Faker faker = new Faker();

    @Autowired
    private GeoServerOperations geoServerOperations;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WMSOverlayRepository wmsOverlayRepository;

    @Autowired
    private PRGOverlayRepository prgOverlayRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SeedProductsTest seedProductsTest;

    @Autowired
    private MockMvc mockMvc;

    private AppUser user;
    private AppUser admin;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        user = appUserRepository.save(AppUser.builder()
                .email(faker.internet().emailAddress("user"))
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .password("{noop}" + faker.internet().password())
                .enabled(true)
                .build());

        admin = appUserRepository.save(AppUser.builder()
                .email(faker.internet().emailAddress("admin"))
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .password("{noop}" + faker.internet().password())
                .enabled(true)
                .admin(true)
                .build());

        for (String workspace : geoServerOperations.listWorkspaces()) {
            geoServerOperations.deleteWorkspace(workspace, true);
        }
    }

    @Nested
    class ResetWorkspace {
        @Test
        public void shouldWork() throws Exception {
            assertThat(geoServerOperations.listWorkspaces(), hasSize(0));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/reset-workspace")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(geoServerOperations.listWorkspaces(), contains("test"));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            assertThat(geoServerOperations.listWorkspaces(), hasSize(0));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/reset-workspace")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(geoServerOperations.listWorkspaces(), hasSize(0));
        }

        @Nested
        class WithPreexistingWorkspaces {
            @BeforeEach
            public void beforeEach() {
                geoServerOperations.createWorkspace("test");
                geoServerOperations.createWorkspace("other");
                seedProductsTest.prepareDb();
                geoServerOperations.createS3CoverageStore("test", "setvak");
            }

            @Test
            public void shouldWork() throws Exception {
                assertThat(geoServerOperations.listWorkspaces(), containsInAnyOrder("test", "other"));
                assertThat(geoServerOperations.listCoverageStores("test"), contains("setvak"));

                mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/reset-workspace")
                        .with(jwtBearerToken(admin, objectMapper)))
                        .andExpect(status().isOk());

                assertThat(geoServerOperations.listWorkspaces(), containsInAnyOrder("test", "other"));
                assertThat(geoServerOperations.listCoverageStores("test"), hasSize(0));
            }
        }

    }

    @Nested
    class SeedOverlays {
        @BeforeEach
        public void beforeEach() {
            geoServerOperations.createWorkspace("test");
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(wmsOverlayRepository.count(), is(equalTo(0L)));
            assertThat(prgOverlayRepository.count(), is(equalTo(0L)));
            assertThat(geoServerOperations.listDataStores("test"), hasSize(0));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/seed-overlays")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(wmsOverlayRepository.findAll(), containsFeaturesInAnyOrder("label"));
            assertThat(prgOverlayRepository.findAll(), containsFeaturesInAnyOrder("featureType"));
            for (PRGOverlay prgOverlay : prgOverlayRepository.findAll()) {
                assertThat(prgOverlay.isCreated(), is(true));
            }
            assertThat(geoServerOperations.listDataStores("test"), contains("prg"));
        }

        @Test
        public void shouldAllowToSkipSyncGeoserver() throws Exception {
            assertThat(wmsOverlayRepository.count(), is(equalTo(0L)));
            assertThat(prgOverlayRepository.count(), is(equalTo(0L)));
            assertThat(geoServerOperations.listDataStores("test"), hasSize(0));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/seed-overlays")
                    .param("syncGeoserver", "false")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(wmsOverlayRepository.findAll(), containsFeaturesInAnyOrder("label"));
            assertThat(prgOverlayRepository.findAll(), containsFeaturesInAnyOrder("featureType"));
            for (PRGOverlay prgOverlay : prgOverlayRepository.findAll()) {
                assertThat(prgOverlay.isCreated(), is(true));
            }
            assertThat(geoServerOperations.listDataStores("test"), hasSize(0));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            assertThat(wmsOverlayRepository.count(), is(equalTo(0L)));
            assertThat(prgOverlayRepository.count(), is(equalTo(0L)));
            assertThat(geoServerOperations.listDataStores("test"), hasSize(0));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/seed-overlays")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(wmsOverlayRepository.count(), is(equalTo(0L)));
            assertThat(prgOverlayRepository.count(), is(equalTo(0L)));
            assertThat(geoServerOperations.listDataStores("test"), hasSize(0));
        }

        private Matcher<Iterable<?>> containsFeaturesInAnyOrder(String propertyName) {
            return containsInAnyOrder(
                    hasProperty(propertyName, equalTo("wojewodztwa")),
                    hasProperty(propertyName, equalTo("powiaty")),
                    hasProperty(propertyName, equalTo("gminy")),
                    hasProperty(propertyName, equalTo("jednostki_ewidencyjne")),
                    hasProperty(propertyName, equalTo("obreby_ewidencyjne"))
            );
        }
    }

    @Nested
    class SynchronizeProductLayers {
        @BeforeEach
        public void beforeEach() {
            geoServerOperations.createWorkspace("test");
            seedProductsTest.prepareDb();
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(geoServerOperations.layerExists("test", "108m"), is(false));
            assertThat(geoServerOperations.layerExists("test", "setvak"), is(false));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/product-layers/synchronize", "108m")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(geoServerOperations.layerExists("test", "108m"), is(true));
            assertThat(geoServerOperations.layerExists("test", "setvak"), is(true));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            assertThat(geoServerOperations.layerExists("test", "108m"), is(false));
            assertThat(geoServerOperations.layerExists("test", "setvak"), is(false));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/product-layers/synchronize", "108m")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(geoServerOperations.layerExists("test", "108m"), is(false));
            assertThat(geoServerOperations.layerExists("test", "setvak"), is(false));
        }
    }

    @Nested
    class CreateProductLayer {
        @BeforeEach
        public void beforeEach() {
            geoServerOperations.createWorkspace("test");
            seedProductsTest.prepareDb();
        }

        @Test
        public void shouldWork() throws Exception {
            assertThat(geoServerOperations.layerExists("test", "108m"), is(false));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/product-layers/{layerName}", "108m")
                    .with(jwtBearerToken(admin, objectMapper)))
                    .andExpect(status().isOk());

            assertThat(geoServerOperations.layerExists("test", "108m"), is(true));
        }

        @Test
        public void shouldBeSecured() throws Exception {
            assertThat(geoServerOperations.layerExists("test", "108m"), is(false));

            mockMvc.perform(post(ADMIN_PREFIX + "/geoserver/product-layers/{layerName}", "108m")
                    .with(jwtBearerToken(user, objectMapper)))
                    .andExpect(status().isForbidden());

            assertThat(geoServerOperations.layerExists("test", "108m"), is(false));
        }
    }
}
