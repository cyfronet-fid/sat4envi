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
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.mapper.SceneMapper;
import pl.cyfronet.s4e.controller.request.ZoneParameter;
import pl.cyfronet.s4e.controller.response.MostRecentSceneResponse;
import pl.cyfronet.s4e.controller.response.SceneResponse;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.SceneService;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;
import pl.cyfronet.s4e.util.TimeHelper;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "scene", description = "The Scene API")
public class SceneController {
    private final SceneService sceneService;
    private final SceneStorage sceneStorage;
    private final TimeHelper timeHelper;
    private final SceneMapper sceneMapper;

    @Operation(summary = "View a list of scenes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping("/products/{id}/scenes")
    public List<SceneResponse> getScenes(
            @PathVariable(name = "id") Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "UTC") ZoneParameter timeZone
    ) throws NotFoundException {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();

        ZonedDateTime zdtStart = ZonedDateTime.of(date, LocalTime.MIDNIGHT, timeZone.getZoneId());
        LocalDateTime start = timeHelper.getLocalDateTimeInBaseZone(zdtStart);
        LocalDateTime end = timeHelper.getLocalDateTimeInBaseZone(zdtStart.plusDays(1));

        return sceneService.list(productId, start, end, userDetails, SceneResponse.Projection.class).stream()
                .map(s -> sceneMapper.toResponse(s, timeZone.getZoneId()))
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
            @RequestParam(defaultValue = "UTC") ZoneParameter timeZone
    ) {
        return sceneService.getAvailabilityDates(productId, yearMonth, timeZone.getZoneId());
    }

    public interface SceneProjection extends ProjectionWithId {
        LocalDateTime getTimestamp();
    }

    @Operation(summary = "Return the most recent available Scene for a Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved most recent scene or Product has no Scenes"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping("/products/{productId}/scenes/most-recent")
    public MostRecentSceneResponse getMostRecent(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "UTC") ZoneParameter timeZone
    ) throws NotFoundException {
        val userDetails = AppUserDetailsSupplier.get();
        return sceneService.getMostRecentScene(productId, userDetails, SceneProjection.class)
                .map(scene -> MostRecentSceneResponse.builder()
                        .sceneId(scene.getId())
                        .timestamp(timeHelper.getZonedDateTime(scene.getTimestamp(), timeZone.getZoneId()))
                        .build())
                .orElseGet(() -> MostRecentSceneResponse.builder().build());
    }

    @Operation(summary = "Redirect to a presigned download url for a scene's artifact")
    @ApiResponses({
            @ApiResponse(responseCode = "303", description = "Redirect to the presigned download url", content = @Content,
                    headers = @Header(name = "Location", description = "The presigned download url")),
            @ApiResponse(responseCode = "404", description = "Scene or artifact not found", content = @Content)
    })
    @GetMapping(value = "/scenes/{id}/download/{artifactName}")
    public ResponseEntity<Void> generateDownloadLink(@PathVariable Long id, @PathVariable String artifactName)
            throws NotFoundException, URISyntaxException {
        URL downloadUrl = sceneStorage.generatePresignedGetLinkWithFileType(
                id,
                artifactName,
                sceneStorage.getPresignedGetTimeout()
        );
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(downloadUrl.toURI()).build();
    }
}
