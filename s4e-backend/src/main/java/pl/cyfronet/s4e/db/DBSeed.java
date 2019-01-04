package pl.cyfronet.s4e.db;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.granules.Granule;
import pl.cyfronet.s4e.granules.GranuleRepository;
import pl.cyfronet.s4e.products.Product;
import pl.cyfronet.s4e.products.ProductRepository;
import pl.cyfronet.s4e.user.AppRole;
import pl.cyfronet.s4e.user.AppUser;
import pl.cyfronet.s4e.user.AppUserRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DBSeed {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 0, 0);

    private final ProductRepository productRepository;
    private final GranuleRepository granuleRepository;
    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seed() {
        if (productRepository.count() > 0 || granuleRepository.count() > 0 || appUserRepository.count() > 0) {
            return;
        }
        List<Product> products = Arrays.asList(new Product[]{
                Product.builder()
                        .name("108m")
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .build(),
                Product.builder()
                        .name("WV-IR")
                        .build(),
        });
        productRepository.saveAll(products);

        val granules = new ArrayList<Granule>();
        granules.addAll(generateGranules(products.get(0), "test:", "_Merkator_Europa_ir_108m"));
        granules.addAll(generateGranules(products.get(1), "test:", "_Merkator_Europa_ir_108_setvak"));
        granules.addAll(generateGranules(products.get(2), "test:", "_Merkator_WV-IR"));
        granuleRepository.saveAll(granules);

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

    private List<Granule> generateGranules(Product product, String prefix, String suffix) {
        val count = 24;
        val granules = new ArrayList<Granule>();
        for (int i = 0; i < count; i++) {
            LocalDateTime timestamp = BASE_TIME.plusHours(i);
            granules.add(Granule.builder()
                    .product(product)
                    .timestamp(timestamp)
                    .layerName(prefix + DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(timestamp) + suffix)
                    .build());
        }
        return granules;
    }
}
