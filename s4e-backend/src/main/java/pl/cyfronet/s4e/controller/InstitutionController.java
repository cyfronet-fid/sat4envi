package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.controller.response.InstitutionResponse;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InstitutionService;

import javax.validation.Valid;

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
            @ApiResponse(responseCode = "400", description = "Institution not created"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to create an institution")
    })
    @PostMapping(value = "/institutions", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isAdmin()")
    public void create(@RequestBody @Valid CreateInstitutionRequest request) throws InstitutionCreationException {
        institutionService.save(request);
    }

    @Operation(summary = "Create a new child institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was created"),
            @ApiResponse(responseCode = "400", description = "Institution not created"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to create an institution")
    })
    @PostMapping(value = "/institutions/{institution}/child", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isInstitutionAdmin(#institutionSlug)")
    public void createChild(@RequestBody @Valid CreateChildInstitutionRequest request,
                                         @PathVariable("institution") String institutionSlug)
            throws InstitutionCreationException, NotFoundException {
        institutionService.createChildInstitution(request, institutionSlug);
    }

    @Operation(summary = "Get a list of institutions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get list")
    })
    @PageableAsQueryParam
    @GetMapping("/institutions")
    @PreAuthorize("isAuthenticated() && isAdmin()")
    public Page<InstitutionResponse> getAll(@Parameter(hidden = true) Pageable pageable) {
        return institutionService.getAll(pageable, InstitutionResponse.class);
    }

    @Operation(summary = "Get an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved an institution"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get an institution"),
            @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    @GetMapping("/institutions/{institution}")
    @PreAuthorize("isAuthenticated() && isInstitutionMember(#institutionSlug)")
    public InstitutionResponse get(@PathVariable("institution") String institutionSlug) throws NotFoundException {
        return institutionService.getInstitution(institutionSlug, InstitutionResponse.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));
    }

    @Operation(summary = "Update an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was updated"),
            @ApiResponse(responseCode = "400", description = "Institution not updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to update an institution"),
            @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    @PutMapping(value = "/institutions/{institution}", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isInstitutionManager(#institutionSlug)")
    public void update(@RequestBody UpdateInstitutionRequest request,
                                    @PathVariable("institution") String institutionSlug)
            throws NotFoundException, InstitutionUpdateException {
        institutionService.update(request, institutionSlug);
    }

    @Operation(summary = "Delete an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to delete an institution")
    })
    @DeleteMapping("/institutions/{institution}")
    @PreAuthorize("isAuthenticated() && isInstitutionAdmin(#institutionSlug)")
    public void delete(@PathVariable("institution") String institutionSlug) {
        institutionService.delete(institutionSlug);
    }
}
