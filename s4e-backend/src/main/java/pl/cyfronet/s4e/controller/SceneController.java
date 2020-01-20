package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.ZoneParameter;
import pl.cyfronet.s4e.controller.response.SceneResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.SceneService;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.util.TimeHelper;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.*;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "scene", description = "The Scene API")
public class SceneController {
    private final SceneService sceneService;
    private final SceneStorage sceneStorage;
    private final TimeHelper timeHelper;

    @Operation(summary = "View a list of scenes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/products/{id}/scenes")
    public List<SceneResponse> getScenes(
            @PathVariable(name = "id") Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "UTC") ZoneParameter tz
    ) {
        ZonedDateTime zdtStart = ZonedDateTime.of(date, LocalTime.MIDNIGHT, tz.getZoneId());
        LocalDateTime start = timeHelper.getLocalDateTimeInBaseZone(zdtStart);
        LocalDateTime end = timeHelper.getLocalDateTimeInBaseZone(zdtStart.plusDays(1));
        return sceneService.getScenes(productId, start, end).stream()
                .map(s -> SceneResponse.of(s, tz.getZoneId(), timeHelper))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Return days on which Product is available")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/products/{id}/scenes/available")
    public List<LocalDate> getAvailabilityDates(
            @PathVariable(name = "id") Long productId,
            @Parameter(schema = @Schema(type = "string", example = "2019-12")) @RequestParam YearMonth yearMonth,
            @RequestParam(defaultValue = "UTC") ZoneParameter tz
    ) {
        return sceneService.getAvailabilityDates(productId, yearMonth, tz.getZoneId());
    }

    @Operation(summary = "Redirect to a presigned download url for a scene")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to the presigned download url", content = @Content,
                    headers = @Header(name = "Location", description = "The presigned download url")),
            @ApiResponse(responseCode = "404", description = "Scene not found", content = @Content)
    })
    @GetMapping(value = "/scenes/{id}/download")
    public ResponseEntity<Void> generateDownloadLink(@PathVariable Long id) throws NotFoundException, URISyntaxException {
        URL downloadLink = sceneStorage.generatePresignedGetLink(id, sceneStorage.getPresignedGetTimeout());
        return ResponseEntity.status(HttpStatus.FOUND).location(downloadLink.toURI()).build();
    }
}
