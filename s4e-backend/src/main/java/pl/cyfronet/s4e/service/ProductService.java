package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.controller.response.BasicProductResponse;
import pl.cyfronet.s4e.controller.response.ProductResponse;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<BasicProductResponse> getProducts() {
        return productRepository.findAllBy(BasicProductResponse.class);
    }

    public Optional<ProductResponse> getProduct(Long id) {
        return productRepository.findById(id, ProductResponse.class);
    }
}
