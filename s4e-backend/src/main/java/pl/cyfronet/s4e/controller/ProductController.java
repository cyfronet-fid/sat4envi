package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.response.BasicProductResponse;
import pl.cyfronet.s4e.controller.response.ProductResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
@Tag(name = "product", description = "The Product API")
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "View a list of products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/products")
    public List<BasicProductResponse> getProducts() {
        return productService.getProducts().stream()
                .map(BasicProductResponse::of)
                .collect(Collectors.toList());
    }

    @Operation(summary = "View product info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) throws NotFoundException {
        val product = productService.getProduct(id)
                .orElseThrow(() -> new NotFoundException("Product not found for id '" + id));
        return ProductResponse.of(product);
    }
}
