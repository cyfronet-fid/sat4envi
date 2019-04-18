package pl.cyfronet.s4e.db;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
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
public class DBSeedDevelopment {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 0, 0);

    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;
    private final SldStyleRepository sldStyleRepository;
    private final AppUserRepository appUserRepository;

    private final GeoServerService geoServerService;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seed() {
        if (productTypeRepository.count() > 0 || productRepository.count() > 0 || appUserRepository.count() > 0) {
            return;
        }
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

        val products = new ArrayList<Product>();
        products.addAll(generateProducts(productTypes.get(0), "_Merkator_Europa_ir_108m"));
        products.addAll(generateProducts(productTypes.get(1), "_Merkator_Europa_ir_108_setvak"));
        products.addAll(generateProducts(productTypes.get(2), "_Merkator_WV-IR"));
        productRepository.saveAll(products);

        val sldStyles = new ArrayList<SldStyle>();
        sldStyles.add(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        sldStyleRepository.saveAll(sldStyles);

        geoServerService.resetWorkspace();
        for (val product: products) {
            geoServerService.addLayer(product);
        }
        for (val sldStyle: sldStyles) {
            geoServerService.addStyle(sldStyle);
        }

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
