package pl.cyfronet.s4e.admin.sync;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/sync", produces = APPLICATION_JSON_VALUE)
@Tag(name = "admin-scene-sync", description = "The Admin Scenes Sync API")
@RequiredArgsConstructor
@Slf4j
public class AdminSceneSyncController {
    private final AdminSceneSyncService adminSceneSyncService;

    @Operation(summary = "Start scenes sync")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void sync(@RequestBody @Valid AdminSceneSyncRequest request) {
        // should return 200 - ok and some form of indentification to look for in logs in case admin want to check on this task.
        adminSceneSyncService.readScenes(request.getPrefix());
    }
}
