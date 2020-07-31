package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.api.ResponseExtender;
import pl.cyfronet.s4e.controller.response.SearchResponse;
import pl.cyfronet.s4e.ex.QueryException;
import pl.cyfronet.s4e.service.SearchService;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "search", description = "The Scene API")
public class SearchController {
    private final SearchService searchService;
    private final ResponseExtender responseExtender;

    @Operation(summary = "View a list of scenes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
    })
    @GetMapping("/search")
    public List<SearchResponse> getScenes(@RequestParam Map<String, Object> params)
            throws SQLException, QueryException {
        ZoneId zoneId = ZoneId.of(String.valueOf(params.getOrDefault("timeZone", "UTC")));
            return searchService.getScenesBy(params).stream()
                    .map(scene -> responseExtender.toResponse(scene, zoneId))
                    .collect(Collectors.toList());
    }
}
