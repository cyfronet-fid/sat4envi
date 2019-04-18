package pl.cyfronet.s4e.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.response.ProductResponse;
import pl.cyfronet.s4e.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/products/productTypeId/{productTypeId}")
    public List<ProductResponse> getProducts(@PathVariable Long productTypeId) {
        return productService.getProducts(productTypeId).stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }
}