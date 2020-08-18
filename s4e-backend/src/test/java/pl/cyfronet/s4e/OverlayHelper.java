package pl.cyfronet.s4e;

import lombok.val;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.bean.WMSOverlay;
import pl.cyfronet.s4e.controller.request.OverlayRequest;

import java.util.concurrent.atomic.AtomicInteger;

public class OverlayHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private static final String label = "Test %d";
    private static final String url = "host:5000/%d";
    private static final String feature = "feature %d";

    public static OverlayRequest.OverlayRequestBuilder overlayRequestBuilder() {
        val label = nextUnique(OverlayHelper.label);
        val url = nextUnique(OverlayHelper.url);
        return OverlayRequest.builder()
                .label(label)
                .url(url);
    }

    public static WMSOverlay.WMSOverlayBuilder wmsOverlayBuilder() {
        val label = nextUnique(OverlayHelper.label);
        val url = nextUnique(OverlayHelper.url);
        return WMSOverlay.builder()
                .label(label)
                .url(url);
    }

    public static SldStyle.SldStyleBuilder sldStyleBuilder() {
        val name = nextUnique(OverlayHelper.label);
        return SldStyle.builder()
                .name(name)
                .created(true);
    }

    public static PRGOverlay.PRGOverlayBuilder prgOverlayBuilder() {
        val feature = nextUnique(OverlayHelper.feature);
        return PRGOverlay.builder()
                .featureType(feature)
                .created(true);
    }

    private static String nextUnique(String format) {
        return String.format(format, COUNT.getAndIncrement());
    }
}
