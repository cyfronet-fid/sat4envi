package pl.cyfronet.s4e.products;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    public List<Product> getProducts() {
        return Arrays.asList(new Product[] {
                Product.builder()
                        .id(1L)
                        .name("rainfall")
                        .build(),
                Product.builder()
                        .id(2L)
                        .name("clouds")
                        .build(),
        });
    }
}
