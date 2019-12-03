package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getProducts() {
        val out = new ArrayList<Product>();
        productRepository.findAll().forEach(out::add);
        return out;
    }

    public Optional<Product> getProduct(Long id){
        return productRepository.findById(id);
    }
}
