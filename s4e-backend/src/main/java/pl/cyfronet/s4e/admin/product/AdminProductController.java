package pl.cyfronet.s4e.admin.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.product.ProductDeletionException;
import pl.cyfronet.s4e.ex.product.ProductException;
import pl.cyfronet.s4e.service.ProductService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/products", produces = APPLICATION_JSON_VALUE)
@Tag(name = "admin-product", description = "The Admin Product API")
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;
    private final AdminProductMapper adminProductMapper;

    @Operation(summary = "Create Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public AdminProductResponse create(@RequestBody @Valid AdminCreateProductRequest request) throws ProductException {
        ProductService.DTO dto = adminProductMapper.toProductServiceDTO(request);
        Long newId = productService.create(dto);
        return productService.findByIdFetchSchemas(newId, AdminProductResponse.class).get();
    }

    @Operation(summary = "List Products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public List<AdminProductResponse> list() {
        return productService.findAllFetchSchemas(AdminProductResponse.class);
    }

    @Operation(summary = "Return Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product doesn't exist", content = @Content)
    })
    @GetMapping("/{id}")
    public AdminProductResponse read(@PathVariable Long id) throws NotFoundException {
        return productService.findByIdFetchSchemas(id, AdminProductResponse.class)
                .orElseThrow(() -> new NotFoundException("Product not found for id '" + id + "'"));
    }

    @Operation(summary = "Update Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product doesn't exist", content = @Content)
    })
    @PatchMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public AdminProductResponse update(
            @PathVariable Long id,
            @RequestBody @Valid AdminUpdateProductRequest request
    ) throws NotFoundException, ProductException {
        ProductService.DTO dto = adminProductMapper.toProductServiceDTO(request);
        productService.update(id, dto);
        return productService.findByIdFetchSchemas(id, AdminProductResponse.class).get();
    }

    @Operation(summary = "Delete Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "There are existing references to the Product", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product doesn't exist", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws NotFoundException, ProductDeletionException {
        productService.delete(id);
    }
}
