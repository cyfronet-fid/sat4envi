package pl.cyfronet.s4e.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.response.ProductResponse;
import pl.cyfronet.s4e.service.ProductService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
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
    @GetMapping("/products/productTypeId/{productTypeId}")
    public List<ProductResponse> getProducts(
            @PathVariable Long productTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date != null) {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.of(0, 0));
            LocalDateTime end = start.plusDays(1);
            return productService.getProducts(productTypeId, start, end).stream()
                    .map(ProductResponse::of)
                    .collect(Collectors.toList());
        }

        return productService.getProducts(productTypeId).stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Return days on which ProductType is available")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved list")
    })
    @GetMapping("/products/productTypeId/{productTypeId}/available")
    public List<LocalDate> getAvailabilityDates(@PathVariable Long productTypeId, @RequestParam YearMonth yearMonth) {
        return productService.getAvailabilityDates(productTypeId, yearMonth);
    }
}
