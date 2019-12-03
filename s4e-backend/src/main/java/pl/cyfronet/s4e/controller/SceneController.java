package pl.cyfronet.s4e.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.response.SceneResponse;
import pl.cyfronet.s4e.service.SceneService;

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
public class SceneController {
    private final SceneService sceneService;

    @ApiOperation(value = "View a list of scenes")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved list")
    })
    @GetMapping("/scenes/productId/{productId}")
    public List<SceneResponse> getScenes(
            @PathVariable Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date != null) {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.of(0, 0));
            LocalDateTime end = start.plusDays(1);
            return sceneService.getScenes(productId, start, end).stream()
                    .map(SceneResponse::of)
                    .collect(Collectors.toList());
        }

        return sceneService.getScenes(productId).stream()
                .map(SceneResponse::of)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Return days on which Product is available")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved list")
    })
    @GetMapping("/scenes/productId/{productId}/available")
    public List<LocalDate> getAvailabilityDates(@PathVariable Long productId, @RequestParam YearMonth yearMonth) {
        return sceneService.getAvailabilityDates(productId, yearMonth);
    }
}
