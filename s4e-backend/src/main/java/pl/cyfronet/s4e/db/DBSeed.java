package pl.cyfronet.s4e.db;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.granules.Granule;
import pl.cyfronet.s4e.granules.GranuleRepository;
import pl.cyfronet.s4e.products.Product;
import pl.cyfronet.s4e.products.ProductRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DBSeed {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 8, 0);

    private final ProductRepository productRepository;
    private final GranuleRepository granuleRepository;

    @PostConstruct
    public void seed() {
        if (productRepository.count() > 0 || granuleRepository.count() > 0) {
            return;
        }
        List<Product> products = Arrays.asList(new Product[]{
                Product.builder()
                        .name("rainfall")
                        .build(),
                Product.builder()
                        .name("clouds")
                        .build(),
        });
        productRepository.saveAll(products);

        val granules = new ArrayList<Granule>();
        for (Product product: products) {
            val count = 4;
            for (int i = 0; i < count; i++) {
                LocalDateTime timestamp = BASE_TIME.plusMinutes(15*i);
                granules.add(Granule.builder()
                        .product(product)
                        .timestamp(timestamp)
                        .layerName("test:"+ DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(timestamp) +"_Merkator_Europa_ir_108m")
                        .build());
            }
        }
        granuleRepository.saveAll(granules);
    }
}
