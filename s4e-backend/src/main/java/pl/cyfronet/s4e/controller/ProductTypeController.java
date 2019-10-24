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
import pl.cyfronet.s4e.controller.response.BasicProductTypeResponse;
import pl.cyfronet.s4e.controller.response.ProductTypeResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.ProductTypeService;

import java.util.List;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    @ApiOperation(value = "View a list of product types")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved list")
    })
    @GetMapping("/productTypes")
    public List<BasicProductTypeResponse> getProductTypes() {
        return productTypeService.getProductTypes().stream()
                .map(BasicProductTypeResponse::of)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "View product type info")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved product type"),
            @ApiResponse(code = 404, message = "Product type not found")
    })
    @GetMapping("/productTypes/{id}")
    public ProductTypeResponse getProductType(@PathVariable Long id) throws NotFoundException {
        val optionalProductType = productTypeService.getProductType(id)
                .orElseThrow(() -> new NotFoundException("Product type not found for id '" + id));
        return ProductTypeResponse.of(optionalProductType);
    }
}
