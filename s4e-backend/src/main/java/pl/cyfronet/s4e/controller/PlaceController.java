package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.bean.Place;
import pl.cyfronet.s4e.controller.response.PlaceResponse;
import pl.cyfronet.s4e.service.PlaceService;

import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
@Tag(name = "place", description = "The Place API")
public class PlaceController {
    private final PlaceService placeService;

    @Operation(summary = "View a list of places")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved page")
    })
    @PageableAsQueryParam
    @GetMapping("/places")
    public Page<PlaceResponse> find(@RequestParam String namePrefix, @Parameter(hidden = true) Pageable pageable) {
        Page<Place> page = placeService.findPlace(namePrefix, pageable);
        return new PageImpl<>(
                page.stream()
                        .map(m -> PlaceResponse.of(m))
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }

}
