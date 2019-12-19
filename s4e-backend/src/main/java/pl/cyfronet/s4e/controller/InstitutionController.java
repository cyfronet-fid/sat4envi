package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.controller.response.InstitutionResponse;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.SlugService;

import javax.validation.Valid;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
@Tag(name = "institution", description = "The Institution API")
@PreAuthorize("isAuthenticated()")
public class InstitutionController {
    private final InstitutionService institutionService;
    private final SlugService slugService;

    @Operation(summary = "Create a new institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was created"),
            @ApiResponse(responseCode = "400", description = "Institution not created"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to create an institution")
    })
    @PostMapping("/institutions")
    @PreAuthorize("isAdmin()")
    public ResponseEntity<?> create(@RequestBody @Valid CreateInstitutionRequest request) throws InstitutionCreationException {
        institutionService.save(Institution.builder().name(request.getName()).slug(slugService.slugify(request.getName())).build());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get a list of institutions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get list")
    })
    @GetMapping("/institutions")
    @PreAuthorize("isAdmin()")
    public Page<InstitutionResponse> getAll(Pageable pageable) {
        Page<Institution> page = institutionService.getAll(pageable);
        return new PageImpl<>(
                page.stream()
                        .map(m -> InstitutionResponse.of(m))
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }

    @Operation(summary = "Get an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved an institution"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to get an institution"),
            @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    @GetMapping("/institutions/{institution}")
    @PreAuthorize("isInstitutionMember(#institutionSlug)")
    public InstitutionResponse get(@PathVariable("institution") String institutionSlug) throws NotFoundException {
        val institution = institutionService.getInstitution(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug));
        return InstitutionResponse.of(institution);
    }

    @Operation(summary = "Update an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was updated"),
            @ApiResponse(responseCode = "400", description = "Institution not updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to update an institution"),
            @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    @PutMapping("/institutions/{institution}")
    @PreAuthorize("isInstitutionManager(#institutionSlug)")
    public ResponseEntity<?> update(@RequestBody UpdateInstitutionRequest request,
                                    @PathVariable("institution") String institutionSlug)
            throws NotFoundException, InstitutionUpdateException {
        val institution = institutionService.getInstitution(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug));
        institution.setName(request.getName());
        institution.setSlug(slugService.slugify(request.getName()));
        institutionService.update(institution);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete an institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If institution was deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to delete an institution")
    })
    @DeleteMapping("/institutions/{institution}")
    @PreAuthorize("isInstitutionAdmin(#institutionSlug)")
    public ResponseEntity<?> delete(@PathVariable("institution") String institutionSlug) {
        institutionService.delete(institutionSlug);
        return ResponseEntity.ok().build();
    }
}
