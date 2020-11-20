package pl.cyfronet.s4e.admin.license;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.ex.LicenseGrantException;
import pl.cyfronet.s4e.ex.NotFoundException;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/license-grants", produces = APPLICATION_JSON_VALUE)
@Tag(name = "admin-license", description = "The Admin LicenseGrant API")
@RequiredArgsConstructor
public class AdminLicenseGrantController {
    private final LicenseGrantService licenseGrantService;
    private final AdminLicenseGrantMapper adminLicenseGrantMapper;

    @Operation(summary = "Create LicenseGrant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public AdminLicenseGrantResponse create(@RequestBody @Valid AdminCreateLicenseGrantRequest request) throws LicenseGrantException {
        LicenseGrantService.CreateDTO createDTO = adminLicenseGrantMapper.toCreateDTO(request);
        Long newId = licenseGrantService.create(createDTO);
        return licenseGrantService.findByIdFetchInstitutionAndProduct(newId, AdminLicenseGrantResponse.class).get();
    }

    @Operation(summary = "List LicenseGrants")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public List<AdminLicenseGrantResponse> list() {
        return licenseGrantService.findAllFetchInstitutionAndProduct(AdminLicenseGrantResponse.class);
    }

    @Operation(summary = "Return LicenseGrant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "LicenseGrant doesn't exist", content = @Content)
    })
    @GetMapping("/{id}")
    public AdminLicenseGrantResponse read(@PathVariable Long id) throws NotFoundException {
        return licenseGrantService.findByIdFetchInstitutionAndProduct(id, AdminLicenseGrantResponse.class)
                .orElseThrow(() -> constructNFE(id));
    }

    @Operation(summary = "Update LicenseGrant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "LicenseGrant doesn't exist", content = @Content)
    })
    @PatchMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public AdminLicenseGrantResponse update(
            @PathVariable Long id,
            @RequestBody @Valid AdminUpdateLicenseGrantRequest request
    ) throws NotFoundException {
        return licenseGrantService.updateOwner(id, request.isOwner(), AdminLicenseGrantResponse.class)
                .orElseThrow(() -> constructNFE(id));
    }

    @Operation(summary = "Delete LicenseGrant")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "LicenseGrant doesn't exist", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws NotFoundException {
        if (!licenseGrantService.delete(id)) {
            throw constructNFE(id);
        }
        return ResponseEntity.noContent().build();
    }

    private NotFoundException constructNFE(Long id) {
        return new NotFoundException("LicenseGrant with id '" + id + "' not found");
    }
}
