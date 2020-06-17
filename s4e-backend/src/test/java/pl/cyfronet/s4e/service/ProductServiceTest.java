package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@BasicTest
@Slf4j
public class ProductServiceTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private TestDbHelper testDbHelper;

    private ProductService productService;

    private AppUser appUser;

    private Long productId;

    @BeforeEach
    public void beforeEach() {
        productService = new ProductService(repository, appUserRepository);
        resetDb();

        appUser = appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());

        Map<String, String> leftDesc = new HashMap<>();
        leftDesc.put("0.75", "upper text left");
        leftDesc.put("0.25", "lower text left");
        Map<String, String> rightDesc = new HashMap<>();
        rightDesc.put("0.75", "upper text right");
        rightDesc.put("0.25", "lower text right");
        Legend legend = Legend.builder()
                .type("gradient")
                .url("url")
                .leftDescription(leftDesc)
                .rightDescription(rightDesc)
                .build();

        repository.saveAll(Arrays.asList(Product.builder()
                        .name("108m")
                        .displayName("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .layerName("108m")
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .displayName("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .layerName("setvak")
                        .build(),
                Product.builder()
                        .name("WV_IR")
                        .displayName("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .layerName("wv_ir")
                        .legend(legend)
                        .build()));

        productId = repository.findByNameContainingIgnoreCase("108m").get().getId();
    }

    @AfterEach
    public void afterEach() {
        resetDb();
        SecurityContextHolder.clearContext();
    }

    private void resetDb() {
        testDbHelper.clean();
    }

    @Test
    public void shouldReturnFalseForIsFavourite() {
        authenticateAs(appUser.getEmail());

        assertThat(productService.isFavourite(productId), is(false));
    }

    @Test
    public void shouldReturnTrueForIsFavourite() {
        Product product = repository.findByNameContainingIgnoreCase("108m").get();
        product.setFavourites(new HashSet<>(Arrays.asList(appUser)));
        repository.save(product);

        authenticateAs(appUser.getEmail());

        assertThat(productService.isFavourite(productId), is(true));
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
