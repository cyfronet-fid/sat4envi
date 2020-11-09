package pl.cyfronet.s4e.admin.institution;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.ex.InstitutionZkException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InstitutionService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/institutions", produces = APPLICATION_JSON_VALUE)
@Tag(name = "admin-institution", description = "The Admin Institution API")
@RequiredArgsConstructor
public class AdminInstitutionController {
    private final InstitutionService institutionService;

    @Operation(summary = "Set ZK flag")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ZK flag set"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PatchMapping(path = "/{slug}/zk/set")
    public void setZk(@PathVariable String slug) throws NotFoundException, InstitutionZkException {
        institutionService.setZk(slug);
    }

    @Operation(summary = "Unset ZK flag")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ZK flag unset"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PatchMapping(path = "/{slug}/zk/unset")
    public void unsetZk(@PathVariable String slug) throws NotFoundException, InstitutionZkException {
        institutionService.unsetZk(slug);
    }
}
