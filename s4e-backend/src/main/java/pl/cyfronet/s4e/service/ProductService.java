package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getProducts(Long productTypeId) {
        return productRepository.findByProductTypeId(productTypeId);
    }
}
