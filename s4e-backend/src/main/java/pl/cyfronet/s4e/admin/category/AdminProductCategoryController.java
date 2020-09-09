package pl.cyfronet.s4e.admin.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.bean.ProductCategory;
import pl.cyfronet.s4e.data.repository.ProductCategoryRepository;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/product-category", produces = APPLICATION_JSON_VALUE)
@Tag(name = "admin-product-category", description = "The Admin Product Category API")
@RequiredArgsConstructor
public class AdminProductCategoryController {
    private final ProductCategoryRepository productCategoryRepository;

    @Operation(summary = "Create Product Categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping("/seed")
    public void createCategories() {
        productCategoryRepository.deleteAllByNameNot(ProductCategoryRepository.DEFAULT_CATEGORY_NAME);

        List<ProductCategory> productCategories = Arrays.asList(
                ProductCategory.builder()
                        .label("Atmosfera/Meteorologia")
                        .name("atmosphere-meteorology")
                        .iconName("ico_cloud")
                        .build(),
                ProductCategory.builder()
                        .label("Morze")
                        .name("sea")
                        .iconName("icon_sea")
                        .build(),
                ProductCategory.builder()
                        .label("Powietrze")
                        .name("air")
                        .iconName("icon_air")
                        .build(),
                ProductCategory.builder()
                        .label("Deszcze i burze")
                        .name("rains-and-storms")
                        .iconName("ico_cloud")
                        .build(),
                ProductCategory.builder()
                        .label("Powierzchnia")
                        .name("surface")
                        .iconName("ico_earth")
                        .build(),
                ProductCategory.builder()
                        .label("Powierzchnia/Meteorologia")
                        .name("surface-meteorology")
                        .iconName("ico_earth")
                        .build(),
                ProductCategory.builder()
                        .label("Specjalne")
                        .name("special")
                        .iconName("icon_special")
                        .build()
        );
        productCategoryRepository.saveAll(productCategories);
    }
}
