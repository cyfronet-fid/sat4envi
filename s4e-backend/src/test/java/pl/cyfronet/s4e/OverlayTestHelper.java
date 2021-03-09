/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e;

import lombok.val;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.bean.WMSOverlay;
import pl.cyfronet.s4e.controller.request.OverlayRequest;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class OverlayTestHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private static final String label = "Test %d";
    private static final String url = "host:5000/%d";
    private static final String feature = "feature %d";

    public static OverlayRequest.OverlayRequestBuilder overlayRequestBuilder() {
        val label = nextUnique(OverlayTestHelper.label);
        val url = nextUnique(OverlayTestHelper.url);
        return OverlayRequest.builder()
                .label(label)
                .url(url);
    }

    public static WMSOverlay.WMSOverlayBuilder wmsOverlayBuilder() {
        val label = nextUnique(OverlayTestHelper.label);
        val url = nextUnique(OverlayTestHelper.url);
        return WMSOverlay.builder()
                .label(label)
                .layerName(label)
                .url(url);
    }

    public static SldStyle.SldStyleBuilder sldStyleBuilder() {
        val name = nextUnique(OverlayTestHelper.label);
        return SldStyle.builder()
                .name(name)
                .created(true);
    }

    public static PRGOverlay.PRGOverlayBuilder prgOverlayBuilder() {
        val feature = nextUnique(OverlayTestHelper.feature);
        return PRGOverlay.builder()
                .featureType(feature)
                .created(true);
    }

    private static String nextUnique(String format) {
        return String.format(Locale.ENGLISH, format, COUNT.getAndIncrement());
    }
}
