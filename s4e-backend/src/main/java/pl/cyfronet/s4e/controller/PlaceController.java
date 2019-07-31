package pl.cyfronet.s4e.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
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
public class PlaceController {
    private final PlaceService placeService;

    @ApiOperation(value = "View a list of places")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved page")
    })
    @GetMapping("/places")
    public Page<PlaceResponse> find(@RequestParam String namePrefix, Pageable pageable) {
        Page<Place> page = placeService.findPlace(namePrefix, pageable);
        return new PageImpl<>(
                page.stream()
                        .map(m -> PlaceResponse.of(m))
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }

}
