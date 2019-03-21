package pl.cyfronet.s4e.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.response.ProductTypeResponse;
import pl.cyfronet.s4e.service.ProductTypeService;

import java.util.List;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    @GetMapping("/productTypes")
    public List<ProductTypeResponse> getProductTypes() {
        return productTypeService.getProductTypes().stream()
                .map(ProductTypeResponse::of)
                .collect(Collectors.toList());
    }
}
