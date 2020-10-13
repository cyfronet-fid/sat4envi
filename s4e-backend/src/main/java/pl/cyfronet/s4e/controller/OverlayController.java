package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.OverlayOwner;
import pl.cyfronet.s4e.bean.WMSOverlay;
import pl.cyfronet.s4e.controller.request.OverlayRequest;
import pl.cyfronet.s4e.controller.response.OverlayResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.AppUserService;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.OverlayService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "overlay", description = "The Overlay API")
public class OverlayController {
    private final OverlayService overlayService;
    private final AppUserService appUserService;
    private final InstitutionService institutionService;

    @Operation(summary = "View a list of MS overlays")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/overlays")
    public List<OverlayResponse> get() throws NotFoundException {
        val appUserDetails = AppUserDetailsSupplier.get();
        if (appUserDetails == null) {
            return overlayService.findAllGlobal();
        }

        val user = appUserService.findByEmail(AppUserDetailsSupplier.get().getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: '"
                        + appUserDetails.getUsername() + "'"
                ));
        val globalOverlays = overlayService.findAllGlobalByUser(user);
        val institutionalOverlays = overlayService.findAllInstitutionalByUser(user);
        val personalOverlays = overlayService.findAllPersonalByUser(user);

        return Stream.of(globalOverlays, institutionalOverlays, personalOverlays)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Add new personal WMS overlay")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been created"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping("/overlays/personal")
    public OverlayResponse createPersonal(@RequestBody @Valid OverlayRequest request) throws NotFoundException {
        val appUser = appUserService.findByEmail(AppUserDetailsSupplier.get().getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + AppUserDetailsSupplier.get().getUsername() + "'"));
        return overlayService.save(
                WMSOverlay.builder()
                    .url(request.getUrl())
                    .label(request.getLabel())
                    .ownerType(OverlayOwner.PERSONAL)
                    .appUser(appUser)
                    .build()
        );
    }

    @Operation(summary = "Add new global WMS overlay")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been created"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping("/overlays/global")
    public OverlayResponse createGlobal(@RequestBody @Valid OverlayRequest request) throws NotFoundException {
        return overlayService.save(
                WMSOverlay.builder()
                        .url(request.getUrl())
                        .label(request.getLabel())
                        .ownerType(OverlayOwner.GLOBAL)
                        .build()
        );
    }

    @Operation(summary = "Add new institutional WMS overlay")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been created"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PostMapping("/institutions/{institution}/overlays")
    public OverlayResponse createInstitutional(
            @RequestBody @Valid OverlayRequest request,
            @PathVariable("institution") String institutionSlug
    ) throws NotFoundException {
        val institution = institutionService.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution with slug: '"
                        + institutionSlug + "' doesn't exist"
                ));
        return overlayService.save(
                WMSOverlay.builder()
                        .url(request.getUrl())
                        .label(request.getLabel())
                        .ownerType(OverlayOwner.INSTITUTIONAL)
                        .institution(institution)
                        .build()
        );
    }

    @Operation(summary = "Delete global WMS overlay")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been created"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/overlays/global/{id}")
    public void deleteGlobal(@PathVariable Long id) {
        overlayService.deleteByIdAndOwnerType(id, OverlayOwner.GLOBAL);
    }

    @Operation(summary = "Delete personal WMS overlay")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/overlays/personal/{id}")
    public void deletePersonal(@PathVariable Long id) throws NotFoundException {
        val appUser = appUserService.findByEmail(AppUserDetailsSupplier.get().getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: '"
                        + AppUserDetailsSupplier.get().getUsername() + "'"
                ));
        val overlay = overlayService
                .findByIdAndAppUserIdAndOwnerType(id, appUser, OverlayOwner.PERSONAL, WMSOverlay.class)
                .orElseThrow(() -> new NotFoundException("Overlay with id: " + id + "doesn't exist"));
        overlayService.delete(overlay);
    }

    @Operation(summary = "Delete institutional WMS overlay")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/institutions/{institution}/overlays/{id}")
    public void deleteInstitutional(
            @PathVariable("institution") String institutionSlug,
            @PathVariable Long id
    ) throws NotFoundException {
        val institution = institutionService.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution with slug: '" + institutionSlug + "' doesn't exist"));
        val overlay = overlayService
                .findByIdAndOwnerTypeAndInstitutionId(id, OverlayOwner.INSTITUTIONAL, institution, WMSOverlay.class)
                .orElseThrow(() -> new NotFoundException("Overlay with id: " + id + "doesn't exist"));
        overlayService.delete(overlay);
    }

    @Transactional
    @Operation(summary = "Set overlay as visible")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been deleted"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @PutMapping("/overlays/{id}/visible")
    public void setAsVisible(@PathVariable Long id) throws NotFoundException {
        val appUser = appUserService.findByEmail(AppUserDetailsSupplier.get().getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + AppUserDetailsSupplier.get().getUsername() + "'"));
        val nonVisibleOverlays = appUser.getPreferences().getNonVisibleOverlays();
        if (nonVisibleOverlays.contains(id)) {
            appUser.getPreferences().getNonVisibleOverlays().remove(id);
            appUserService.update(appUser);
        }
    }

    @Transactional
    @Operation(summary = "Set overlay as non visible")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlay has been deleted"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @DeleteMapping("/overlays/{id}/visible")
    public void setAsNonVisible(@PathVariable Long id) throws NotFoundException {
        val appUser = appUserService.findByEmail(AppUserDetailsSupplier.get().getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: '" + AppUserDetailsSupplier.get().getUsername() + "'"));

        val nonVisibleOverlays = appUser.getPreferences().getNonVisibleOverlays();
        if (!nonVisibleOverlays.contains(id)) {
            appUser.getPreferences().getNonVisibleOverlays().add(id);
            appUserService.update(appUser);
        }
    }
}
