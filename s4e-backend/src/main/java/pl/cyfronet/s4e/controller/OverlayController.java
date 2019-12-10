package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.response.PRGOverlayResponse;
import pl.cyfronet.s4e.controller.response.WMSOverlayResponse;
import pl.cyfronet.s4e.service.PRGOverlayService;
import pl.cyfronet.s4e.service.WMSOverlayService;

import java.util.List;
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
@Tag(name = "overlay", description = "The Overlay API")
public class OverlayController {

    private final PRGOverlayService prgOverlayService;
    private final WMSOverlayService wmsOverlayService;

    @Operation(summary = "View a list of PRG overlays")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/overlays/prg/")
    public List<PRGOverlayResponse> getPRGOverlays() {
        return prgOverlayService.getCreatedPRGOverlays().stream()
                .map(PRGOverlayResponse::of)
                .collect(Collectors.toList());
    }

    @Operation(summary = "View a list of WMS overlays")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/overlays/wms/")
    public List<WMSOverlayResponse> getWMSOverlays() {
        return wmsOverlayService.getWMSOverlays().stream()
                .map(WMSOverlayResponse::of)
                .collect(Collectors.toList());
    }
}
