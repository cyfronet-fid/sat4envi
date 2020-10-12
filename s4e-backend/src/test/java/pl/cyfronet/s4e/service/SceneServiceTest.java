package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestClock;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.SceneTestHelper.sceneBuilder;

@BasicTest
@Slf4j
public class SceneServiceTest {
    private interface SceneProjection extends ProjectionWithId { }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SceneService sceneService;

    @Autowired
    private TestClock clock;

    private Product product;

    @BeforeEach
    public void setUp() {
        testDbHelper.clean();
        product = productRepository.save(productBuilder().build());
    }

    @Nested
    class ListWithTimestampRange {
        private List<Scene> scenes;

        @BeforeEach
        public void beforeEach() {
            LocalDateTime base = getBaseTime();
            scenes = sceneRepository.saveAll(
                    Stream.of(
                            base.minus(4, HOURS),
                            base.minus(2, HOURS),
                            base.minus(30, MINUTES)
                    )
                            .map(timestamp -> sceneBuilder(product, timestamp).build())
                            .collect(Collectors.toList())
            );
        }

        @Test
        public void shouldReturnFilteredScenes() throws NotFoundException {
            LocalDateTime start = scenes.get(0).getTimestamp();
            LocalDateTime end = scenes.get(2).getTimestamp();
            List<SceneProjection> returned = sceneService.list(product.getId(), start, end, null, SceneProjection.class);

            assertContains(returned, List.of(0, 1));
        }

        @Test
        public void shouldThrowNFEIfProductDoesntExist() {
            assertThrows(
                    NotFoundException.class,
                    () -> sceneService.list(product.getId() + 1, null, null, null, SceneProjection.class)
            );
        }

        @Nested
        class WithEumetsatLicense {
            private AppUserDetails userDetails;

            @BeforeEach
            public void beforeEach() {
                product.setAccessType(Product.AccessType.EUMETSAT);
                productRepository.save(product);
            }

            @Test
            public void shouldExcludeLicensedScenesForUnauthorized() throws NotFoundException {
                userDetails = null;

                LocalDateTime end = LocalDateTime.from(clock.instant().atZone(ZoneId.of("UTC")));
                LocalDateTime start = end.minusHours(4);

                List<SceneProjection> returned = sceneService.list(product.getId(), start, end, userDetails, SceneProjection.class);

                assertContains(returned, List.of(0, 1));
            }

            @Test
            public void shouldExcludeLicensedScenesForUserWithoutLicense() throws NotFoundException {
                userDetails = userDetailsWithAuthorities();

                LocalDateTime end = LocalDateTime.from(clock.instant().atZone(ZoneId.of("UTC")));
                LocalDateTime start = end.minusHours(4);

                List<SceneProjection> returned = sceneService.list(product.getId(), start, end, userDetails, SceneProjection.class);

                assertContains(returned, List.of(0, 1));
            }

            @Test
            public void shouldIncludeLicensedScenesForLicensedUser() throws NotFoundException {
                userDetails = userDetailsWithAuthorities("LICENSE_EUMETSAT");

                LocalDateTime end = LocalDateTime.from(clock.instant().atZone(ZoneId.of("UTC")));
                LocalDateTime start = end.minusHours(4);

                List<SceneProjection> returned = sceneService.list(product.getId(), start, end, userDetails, SceneProjection.class);

                assertContains(returned, List.of(0, 1, 2));
            }

            @Test
            public void shouldIncludeLicensedScenesForAdmin() throws NotFoundException {
                userDetails = userDetailsWithAuthorities("ROLE_ADMIN");

                LocalDateTime end = LocalDateTime.from(clock.instant().atZone(ZoneId.of("UTC")));
                LocalDateTime start = end.minusHours(4);

                List<SceneProjection> returned = sceneService.list(product.getId(), start, end, userDetails, SceneProjection.class);

                assertContains(returned, List.of(0, 1, 2));
            }
        }

        private void assertContains(List<SceneProjection> returned, List<Integer> expectedIndexes) {
            List<Long> returnedIds = returned.stream()
                    .map(SceneProjection::getId)
                    .collect(Collectors.toUnmodifiableList());
            List<Long> expectedIds = expectedIndexes.stream()
                    .map(scenes::get)
                    .map(Scene::getId)
                    .collect(Collectors.toUnmodifiableList());
            assertThat(returnedIds, contains(expectedIds.toArray()));
        }
    }

    @Nested
    class GetMostRecentScene {
        private LocalDateTime base;

        @BeforeEach
        public void beforeEach() {
            base = getBaseTime();
        }

        @Test
        public void shouldThrowNFEForNonExistentProduct() {
            assertThrows(
                    NotFoundException.class,
                    () -> sceneService.getMostRecentScene(product.getId() + 1, null, ProjectionWithId.class)
            );
        }

        @Test
        public void shouldReturnEmptyOptionalIfThereAreNoScenes() throws NotFoundException {
            val optionalScene = sceneService.getMostRecentScene(product.getId(), null, ProjectionWithId.class);

            assertThat(optionalScene, isEmpty());
        }

        @Test
        public void shouldReturnMostRecent() throws NotFoundException {
            val scenes = saveScenes(
                    base.minus(1, HOURS),
                    base.minus(2, HOURS),
                    base.minus(3, HOURS)
            );

            val optionalScene = sceneService.getMostRecentScene(product.getId(), null, ProjectionWithId.class);

            assertThat(optionalScene, isPresent());
            assertThat(optionalScene.get().getId(), is(equalTo(scenes.get(0).getId())));
        }

        @Test
        public void shouldReturnOneWithSmallerIdIfTied() throws NotFoundException {
            val scenes = saveScenes(base, base);

            val optionalScene = sceneService.getMostRecentScene(product.getId(), null, ProjectionWithId.class);

            assertThat(optionalScene, isPresent());
            assertThat(optionalScene.get().getId(), is(equalTo(scenes.get(0).getId())));
        }

        @Nested
        class WithEumetsatLicense {
            @BeforeEach
            public void beforeEach() {
                product.setAccessType(Product.AccessType.EUMETSAT);
                productRepository.save(product);
            }

            @Test
            public void shouldReturnEmptyOptionalIfThereAreNoScenes() throws NotFoundException {
                val optionalScene = sceneService.getMostRecentScene(product.getId(), null, ProjectionWithId.class);

                assertThat(optionalScene, isEmpty());
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            public void shouldReturnEmptyOptionalIfThereAreNoScenesWithAccess(boolean authorized) throws NotFoundException {
                val userDetails = authorized ? userDetailsWithAuthorities() : null;

                saveScenes(base.minus(30, MINUTES));

                val optionalScene = sceneService.getMostRecentScene(product.getId(), userDetails, ProjectionWithId.class);

                assertThat(optionalScene, isEmpty());
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            public void shouldReturnMostRecent(boolean authorized) throws NotFoundException {
                val userDetails = authorized ? userDetailsWithAuthorities() : null;

                val scenes = saveScenes(
                        base.minus(3, MINUTES),
                        base.minus(1, HOURS),
                        base.minus(2, HOURS)
                );

                val optionalScene = sceneService.getMostRecentScene(product.getId(), userDetails, ProjectionWithId.class);

                assertThat(optionalScene, isPresent());
                assertThat(optionalScene.get().getId(), is(equalTo(scenes.get(1).getId())));
            }

            @ParameterizedTest
            @ValueSource(booleans = { true, false })
            public void shouldReturnOneWithSmallerIdIfTied(boolean authorized) throws NotFoundException {
                val userDetails = authorized ? userDetailsWithAuthorities() : null;

                val scenes = saveScenes(
                        base.minus(1, MINUTES),
                        base.minus(1, HOURS),
                        base.minus(1, HOURS)
                );

                val optionalScene = sceneService.getMostRecentScene(product.getId(), userDetails, ProjectionWithId.class);

                assertThat(optionalScene, isPresent());
                assertThat(optionalScene.get().getId(), is(equalTo(scenes.get(1).getId())));
            }

            @Nested
            class ForALicensedUserOrAdmin {
                @ParameterizedTest
                @ValueSource(strings = { "ROLE_ADMIN", "LICENSE_EUMETSAT" })
                public void shouldReturnMostRecent(String authority) throws NotFoundException {
                    val userDetails = userDetailsWithAuthorities(authority);

                    val scenes = saveScenes(
                            base.minus(3, MINUTES),
                            base.minus(1, HOURS),
                            base.minus(2, HOURS)
                    );

                    val optionalScene = sceneService.getMostRecentScene(product.getId(), userDetails, ProjectionWithId.class);

                    assertThat(optionalScene, isPresent());
                    assertThat(optionalScene.get().getId(), is(equalTo(scenes.get(0).getId())));
                }

                @ParameterizedTest
                @ValueSource(strings = { "ROLE_ADMIN", "LICENSE_EUMETSAT" })
                public void shouldReturnOneWithSmallerIdIfTwoScenesWithEqualTimestamp(String authority) throws NotFoundException {
                    val userDetails = userDetailsWithAuthorities(authority);

                    val scenes = saveScenes(base.minus(1, MINUTES), base.minus(1, MINUTES));

                    val optionalScene = sceneService.getMostRecentScene(product.getId(), userDetails, ProjectionWithId.class);

                    assertThat(optionalScene, isPresent());
                    assertThat(optionalScene.get().getId(), is(equalTo(scenes.get(0).getId())));
                }
            }
        }

        private List<Scene> saveScenes(LocalDateTime... timestamps) {
            return Arrays.stream(timestamps)
                    .map(timestamp -> sceneBuilder(product, timestamp).build())
                    .map(sceneRepository::save)
                    .collect(Collectors.toList());
        }
    }

    private AppUserDetails userDetailsWithAuthorities(String... authoritiesNames) {
        AppUserDetails userDetails = mock(AppUserDetails.class);
        Set<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesNames)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        when(userDetails.getAuthorities()).thenReturn(authorities);
        return userDetails;
    }

    private LocalDateTime getBaseTime() {
        ZonedDateTime zonedDateTime = clock.instant().atZone(ZoneId.of("UTC"));
        return LocalDateTime.from(zonedDateTime);
    }

    @Nested
    class Save {
        @Test
        public void shouldWork() {
            Scene scene = sceneBuilder(product).build();

            assertThat(sceneRepository.count(), is(equalTo(0L)));

            sceneService.save(scene);

            assertThat(sceneRepository.count(), is(equalTo(1L)));
        }
    }
}
