package pl.cyfronet.s4e.products;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getProducts() {
        val out = new ArrayList<Product>();
        productRepository.findAll().forEach(out::add);
        return out;
    }
}
