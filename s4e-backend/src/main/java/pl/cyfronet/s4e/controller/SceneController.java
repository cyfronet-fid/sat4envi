package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.response.SceneResponse;
import pl.cyfronet.s4e.service.SceneService;
import pl.cyfronet.s4e.util.TimeHelper;

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
@Tag(name = "scene", description = "The Scene API")
public class SceneController {
    private final SceneService sceneService;
    private final TimeHelper timeHelper;

    @Operation(summary = "View a list of scenes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
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
                    .map(s -> SceneResponse.of(s, timeHelper))
                    .collect(Collectors.toList());
        }

        return sceneService.getScenes(productId).stream()
                .map(s -> SceneResponse.of(s, timeHelper))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Return days on which Product is available")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/scenes/productId/{productId}/available")
    public List<LocalDate> getAvailabilityDates(@PathVariable Long productId, @RequestParam YearMonth yearMonth) {
        return sceneService.getAvailabilityDates(productId, yearMonth);
    }
}
