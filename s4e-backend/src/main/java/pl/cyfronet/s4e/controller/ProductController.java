package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.response.BasicProductResponse;
import pl.cyfronet.s4e.controller.response.ProductResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.ProductService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
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
        return productService.findAll(BasicProductResponse.class);
    }

    @Operation(summary = "View product info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) throws NotFoundException {
        return productService.findById(id, ProductResponse.class)
                .orElseThrow(() -> new NotFoundException("Product not found for id '" + id));
    }

    @Operation(summary = "Add to favourite Products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully added"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping("/products/{id}/favourite")
    public void addFavourite(@PathVariable Long id) throws NotFoundException {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();
        productService.addFavourite(id, userDetails.getUsername());
    }

    @Operation(summary = "Remove from favourite Products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully removed"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/products/{id}/favourite")
    public void deleteFavourite(@PathVariable Long id) throws NotFoundException {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();
        productService.deleteFavourite(id, userDetails.getUsername());
    }
}
