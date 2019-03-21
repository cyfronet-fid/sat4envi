package pl.cyfronet.s4e.controller;

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
public class OverlayController {

    private final PRGOverlayService prgOverlayService;
    private final WMSOverlayService wmsOverlayService;

    @GetMapping("/overlays/prg/")
    public List<PRGOverlayResponse> getPRGOverlays() {
        return prgOverlayService.getPRGOverlays().stream()
                .map(PRGOverlayResponse::of)
                .collect(Collectors.toList());
    }

    @GetMapping("/overlays/wms/")
    public List<WMSOverlayResponse> getWMSOverlays() {
        return wmsOverlayService.getWMSOverlays().stream()
                .map(WMSOverlayResponse::of)
                .collect(Collectors.toList());
    }
}
