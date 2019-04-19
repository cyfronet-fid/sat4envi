package pl.cyfronet.s4e.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.service.GeoServerService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Profile("development")
@RequiredArgsConstructor
@Slf4j
public class DBSeedDevelopment {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 0, 0);

    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;
    private final AppUserRepository appUserRepository;

    private final GeoServerService geoServerService;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seed() {
        if (productTypeRepository.count() > 0 || productRepository.count() > 0 || appUserRepository.count() > 0) {
            log.info("Skipping seeding");
            return;
        }
        log.info("Seeding DB entities");
        log.debug("Seeding ProductTypes");
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

        log.debug("Seeding Products");
        val products = new ArrayList<Product>();
        products.addAll(generateProducts(productTypes.get(0), "_Merkator_Europa_ir_108m"));
        products.addAll(generateProducts(productTypes.get(1), "_Merkator_Europa_ir_108_setvak"));
        products.addAll(generateProducts(productTypes.get(2), "_Merkator_WV-IR"));
        productRepository.saveAll(products);

        log.debug("Seeding SldStyles");
        val sldStyles = new ArrayList<SldStyle>();
        sldStyles.add(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        sldStyleRepository.saveAll(sldStyles);

        log.debug("Seeding PRGOverlays");
        val prgOverlays = new ArrayList<PRGOverlay>();
        prgOverlays.add(PRGOverlay.builder()
                .name("wojewodztwa")
                .featureType("wojew%C3%B3dztwa")
                .sldStyle(sldStyles.get(0))
                .build());
        prgOverlayRepository.saveAll(prgOverlays);

        log.debug("Seeding AppUsers");
        List<AppUser> appUsers = Arrays.asList(new AppUser[]{
                AppUser.builder()
                        .email("cat1user@mail.pl")
                        .password(passwordEncoder.encode("cat1user"))
                        .role(AppRole.CAT1)
                        .build(),
                AppUser.builder()
                        .email("cat2user@mail.pl")
                        .password(passwordEncoder.encode("cat2user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .build(),
                AppUser.builder()
                        .email("cat3user@mail.pl")
                        .password(passwordEncoder.encode("cat3user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .role(AppRole.CAT3)
                        .build(),
                AppUser.builder()
                        .email("cat4user@mail.pl")
                        .password(passwordEncoder.encode("cat4user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .role(AppRole.CAT3)
                        .role(AppRole.CAT4)
                        .build(),
        });
        appUserRepository.saveAll(appUsers);

        log.info("Seeding GeoServer");
        log.debug("Resetting workspace");
        geoServerService.resetWorkspace();
        log.debug("Creating products");
        for (val product: products) {
            geoServerService.addLayer(product);
        }
        log.debug("Creating styles");
        for (val sldStyle: sldStyles) {
            geoServerService.addStyle(sldStyle);
        }
        log.debug("Creating PRG overlays");
        geoServerService.createPrgOverlays();
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
