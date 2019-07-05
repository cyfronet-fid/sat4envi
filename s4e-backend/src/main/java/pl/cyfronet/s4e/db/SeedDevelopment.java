package pl.cyfronet.s4e.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.geoserver.sync.GeoServerSynchronizer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Profile("development")
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDevelopment implements ApplicationRunner {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 0, 0);

    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;
    private final AppUserRepository appUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    private final PasswordEncoder passwordEncoder;

    private final GeoServerSynchronizer geoServerSynchronizer;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        emailVerificationRepository.deleteAll();
        appUserRepository.deleteAll();
        productRepository.deleteAll();
        productTypeRepository.deleteAll();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();

        seedUsers();
        seedProducts();
        seedOverlays();

        geoServerSynchronizer.resetWorkspace();
        geoServerSynchronizer.synchronizeOverlays();
        geoServerSynchronizer.synchronizeProducts();

        log.info("Seeding complete");
    }

    private void seedUsers() {
        log.info("Seeding AppUsers");
        List<AppUser> appUsers = Arrays.asList(new AppUser[]{
                AppUser.builder()
                        .email("cat1user@mail.pl")
                        .password(passwordEncoder.encode("cat1user"))
                        .role(AppRole.CAT1)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat2user@mail.pl")
                        .password(passwordEncoder.encode("cat2user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat3user@mail.pl")
                        .password(passwordEncoder.encode("cat3user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .role(AppRole.CAT3)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat4user@mail.pl")
                        .password(passwordEncoder.encode("cat4user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .role(AppRole.CAT3)
                        .role(AppRole.CAT4)
                        .enabled(true)
                        .build(),
        });
        appUserRepository.saveAll(appUsers);
    }

    private void seedProducts() {
        log.info("Seeding ProductTypes");
        List<ProductType> productTypes = Arrays.asList(new ProductType[]{
                ProductType.builder()
                        .name("108m")
                        .build(),
                ProductType.builder()
                        .name("Setvak")
                        .build(),
                ProductType.builder()
                        .name("WV-IR")
                        .build(),
        });
        productTypeRepository.saveAll(productTypes);

        log.info("Seeding Products");
        val products = new ArrayList<Product>();
        products.addAll(generateProducts(productTypes.get(0), "_Merkator_Europa_ir_108m"));
        products.addAll(generateProducts(productTypes.get(1), "_Merkator_Europa_ir_108_setvak"));
        products.addAll(generateProducts(productTypes.get(2), "_Merkator_WV-IR"));
        productRepository.saveAll(products);
    }

    private void seedOverlays() {
        log.info("Seeding SldStyles");
        val sldStyles = new ArrayList<SldStyle>();
        sldStyles.add(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        sldStyleRepository.saveAll(sldStyles);

        log.info("Seeding PRGOverlays");
        val prgOverlays = new ArrayList<PRGOverlay>();
        prgOverlays.add(PRGOverlay.builder()
                .name("wojewodztwa")
                .featureType("wojew%C3%B3dztwa")
                .sldStyle(sldStyles.get(0))
                .build());
        prgOverlayRepository.saveAll(prgOverlays);
    }

    private List<Product> generateProducts(ProductType productType, String suffix) {
        val count = 24;
        val granules = new ArrayList<Product>();
        for (int i = 0; i < count; i++) {
            LocalDateTime timestamp = BASE_TIME.plusHours(i);
            String layerName = DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(timestamp) + suffix;
            granules.add(Product.builder()
                    .productType(productType)
                    .timestamp(timestamp)
                    .layerName(layerName)
                    .s3Path(layerName+".tif")
                    .build());
        }
        return granules;
    }
}
