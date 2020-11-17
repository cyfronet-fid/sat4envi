package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.CreateReportTemplateRequest;
import pl.cyfronet.s4e.controller.response.ReportTemplateResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.ReportTemplateService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1 + "/report-templates", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "reportTemplate", description = "The ReportTemplate API")
public class ReportTemplateController {
    private final ReportTemplateService reportTemplateService;

    @Operation(
            summary = "Create a new ReportTemplate",
            description =
                    "Create a new ReportTemplate, which will have the owner set to the authenticated AppUser. " +
                    "The createdAt is set to the current datetime."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content)
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ReportTemplateResponse create(@RequestBody @Valid CreateReportTemplateRequest request) throws NotFoundException {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();
        UUID id = reportTemplateService.create(ReportTemplateService.CreateDTO.builder()
                .ownerEmail(userDetails.getEmail())
                .caption(request.getCaption())
                .notes(request.getNotes())
                .overlayIds(request.getOverlayIds())
                .productId(request.getProductId())
                .build());
        return reportTemplateService.findById(id, ReportTemplateResponse.class)
                // This shouldn't happen, so throw a runtime exception.
                .orElseThrow(() -> new IllegalStateException("ReportTemplateResponse not found for id '" + id +"'"));
    }

    @Operation(summary = "List ReportTemplates of authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content)
    })
    @GetMapping
    public List<ReportTemplateResponse> list() {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();
        return reportTemplateService.listByAppUser(userDetails.getEmail(), ReportTemplateResponse.class);
    }

    @Operation(summary = "Delete a ReportTemplate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable @Parameter(schema = @Schema(format = "uuid")) UUID uuid) {
        reportTemplateService.delete(uuid);
    }
}
