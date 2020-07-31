package pl.cyfronet.s4e.admin.product;

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

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public AdminProductResponse create(@RequestBody @Valid AdminCreateProductRequest request) throws ProductException {
        ProductService.DTO dto = adminProductMapper.toProductServiceDTO(request);
        Long newId = productService.create(dto);
        return productService.findByIdFetchSchemas(newId, AdminProductResponse.class).get();
    }

    @GetMapping
    public List<AdminProductResponse> list() {
        return productService.findAllFetchSchemas(AdminProductResponse.class);
    }

    @GetMapping("/{id}")
    public AdminProductResponse read(@PathVariable Long id) throws NotFoundException {
        return productService.findByIdFetchSchemas(id, AdminProductResponse.class)
                .orElseThrow(() -> new NotFoundException("Product not found for id '" + id + "'"));
    }

    @PatchMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public AdminProductResponse update(
            @PathVariable Long id,
            @RequestBody @Valid AdminUpdateProductRequest request
    ) throws NotFoundException, ProductException {
        ProductService.DTO dto = adminProductMapper.toProductServiceDTO(request);
        productService.update(id, dto);
        return productService.findByIdFetchSchemas(id, AdminProductResponse.class).get();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws NotFoundException, ProductDeletionException {
        productService.delete(id);
    }
}
