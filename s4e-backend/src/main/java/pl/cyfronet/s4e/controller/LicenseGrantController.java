package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.admin.license.LicenseGrantService;
import pl.cyfronet.s4e.controller.response.LicenseGrantResponse;
import pl.cyfronet.s4e.ex.LicenseGrantException;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1 + "/license-grants", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "license-grant", description = "The LicenseGrant API")
public class LicenseGrantController {
    private final LicenseGrantService licenseGrantService;

    @Operation(summary = "List grants for Institution")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Institution not found (admin only)", content = @Content)
    })
    @GetMapping("/institution/{institutionSlug}")
    public List<LicenseGrantResponse> getLicenseGrantsForInstitution(
            @PathVariable String institutionSlug
    ) throws NotFoundException {
        return licenseGrantService.findAllByInstitutionSlugFetchInstitutionAndProduct(
                institutionSlug,
                LicenseGrantResponse.class
        ).orElseThrow(() -> new NotFoundException("Institution with slug '" + institutionSlug + "' not found"));
    }

    @Operation(summary = "List grants for Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping("/product/{productId}")
    public List<LicenseGrantResponse> getLicenseGrantsForProduct(
            @PathVariable Long productId
    ) throws NotFoundException {
        return licenseGrantService.findAllByProductIdFetchInstitutionAndProduct(
                productId,
                LicenseGrantResponse.class
        ).orElseThrow(() -> new NotFoundException("Product with id '" + productId + "' not found"));
    }

    @Operation(summary = "Grant Product access")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access granted"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PostMapping("/product/{productId}/institution/{institutionSlug}")
    public LicenseGrantResponse grantAccessToProductToInstitution(
            @PathVariable Long productId,
            @PathVariable String institutionSlug
    ) throws LicenseGrantException {
        val newId = licenseGrantService.create(LicenseGrantService.CreateDTO.builder()
                .productId(productId)
                .institutionSlug(institutionSlug)
                .owner(false)
                .build());

        return licenseGrantService.findByIdFetchInstitutionAndProduct(newId, LicenseGrantResponse.class).get();
    }

    @Operation(summary = "Deny Product access")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "No LicenseGrant exists for given Institution", content = @Content)
    })
    @DeleteMapping("/product/{productId}/institution/{institutionSlug}")
    public ResponseEntity<?> denyAccessToProductToInstitution(
            @PathVariable Long productId,
            @PathVariable String institutionSlug
    ) throws NotFoundException, LicenseGrantException {
        if (!licenseGrantService.delete(productId, institutionSlug)) {
            throw new NotFoundException(
                    "LicenseGrant with" +
                    " productId '" + productId + "'" +
                    " and institutionSlug '" + institutionSlug + "'" +
                    " not found"
            );
        }
        return ResponseEntity.noContent().build();
    }
}
