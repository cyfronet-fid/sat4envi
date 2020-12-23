/*
 * Copyright 2020 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.geoserver.op.request;

import lombok.Builder;
import lombok.Value;


/**
 * <pre>
 *     {
 *         "layer": {
 *             "defaultStyle": {
 *                 "name": "{defaultStyle}"
 *             }
 *         }
 *     }
 * </pre>
 */
@Value
public class SetLayerDefaultStyleRequest {
    @Value
    @Builder
    private static class DefaultStyle {
        String name;
    }

    @Value
    @Builder
    private static class Layer {
        DefaultStyle defaultStyle;
    }

    Layer layer;

    public SetLayerDefaultStyleRequest(String workspace, String defaultStyle) {
        layer = Layer.builder()
                .defaultStyle(DefaultStyle.builder()
                        .name(workspace+":"+defaultStyle)
                        .build())
                .build();
    }
}
