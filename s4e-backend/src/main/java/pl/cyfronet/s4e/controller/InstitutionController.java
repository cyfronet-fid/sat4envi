package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.data.rest.converters.PageableAsQueryParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.controller.response.BasicInstitutionResponse;
import pl.cyfronet.s4e.controller.response.InstitutionResponse;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.InstitutionService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "institution", description = "The Institution API")
public class InstitutionController {
    private final InstitutionService institutionService;

    @Operation(summary = "Create a new institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was created"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(value = "/institutions", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isAdmin()")
    public void create(@RequestBody @Valid CreateInstitutionRequest request)
            throws InstitutionCreationException, NotFoundException {
        institutionService.save(request);
    }

    @Operation(summary = "Update an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was updated"),
            @ApiResponse(responseCode = "400", description = "Incorrect request: not updated", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping(value = "/institutions/{institution}", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && " +
            "(isInstitutionAdmin(#institutionSlug)||isInstitutionManager(#institutionSlug)||isAdmin())")
    public void update(@RequestBody UpdateInstitutionRequest request,
                       @PathVariable("institution") String institutionSlug)
            throws NotFoundException, InstitutionUpdateException, S3ClientException {
        institutionService.update(request, institutionSlug);
    }

    @Operation(summary = "Create a new child institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was created"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping(value = "/institutions/{institution}/child", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && (isInstitutionAdmin(#institutionSlug)||isAdmin())")
    public void createChild(@RequestBody @Valid CreateChildInstitutionRequest request,
                            @PathVariable("institution") String institutionSlug)
            throws InstitutionCreationException, NotFoundException {
        institutionService.createChildInstitution(request, institutionSlug);
    }

    @Operation(summary = "Get a list of institutions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
    })
    @PageableAsQueryParam
    @GetMapping("/institutions")
    @PreAuthorize("isAuthenticated()")
    public List<BasicInstitutionResponse> getAll() {
        AppUserDetails appUserDetails =
                (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (appUserDetails.isAdmin()) {
            return institutionService.getAll(BasicInstitutionResponse.class);
        }
        return institutionService.getUserInstitutionsBy(appUserDetails.getUsername(),
                List.of(AppRole.GROUP_MEMBER.name()),
                BasicInstitutionResponse.class);
    }

    @Operation(summary = "Get an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved an institution"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/institutions/{institution}")
    @PreAuthorize("isAuthenticated() && isInstitutionMember(#institutionSlug)")
    public InstitutionResponse get(@PathVariable("institution") String institutionSlug) throws NotFoundException {
        return institutionService.getInstitution(institutionSlug, InstitutionResponse.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));
    }

    @Operation(summary = "Delete an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was deleted"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/institutions/{institution}")
    @PreAuthorize("isAuthenticated() && (isInstitutionAdmin(#institutionSlug)||isAdmin())")
    public void delete(@PathVariable("institution") String institutionSlug) {
        institutionService.delete(institutionSlug);
    }
}
