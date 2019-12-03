package pl.cyfronet.s4e.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
public class ProductController {
    private final ProductService productService;

    @ApiOperation(value = "View a list of products")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved list")
    })
    @GetMapping("/products")
    public List<BasicProductResponse> getProducts() {
        return productService.getProducts().stream()
                .map(BasicProductResponse::of)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "View product info")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved product"),
            @ApiResponse(code = 404, message = "Product not found")
    })
    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) throws NotFoundException {
        val product = productService.getProduct(id)
                .orElseThrow(() -> new NotFoundException("Product not found for id '" + id));
        return ProductResponse.of(product);
    }
}
