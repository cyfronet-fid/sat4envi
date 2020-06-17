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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "overlay", description = "The Overlay API")
public class OverlayController {
    private final PRGOverlayService prgOverlayService;
    private final WMSOverlayService wmsOverlayService;

    @Operation(summary = "View a list of PRG overlays")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/overlays/prg")
    public List<PRGOverlayResponse> getPRGOverlays() {
        return prgOverlayService.getCreatedPRGOverlays();
    }

    @Operation(summary = "View a list of WMS overlays")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping("/overlays/wms")
    public List<WMSOverlayResponse> getWMSOverlays() {
        return wmsOverlayService.getWMSOverlays();
    }
}
