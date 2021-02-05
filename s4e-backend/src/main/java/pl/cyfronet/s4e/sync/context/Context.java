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

package pl.cyfronet.s4e.sync.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.Prototype;
import pl.cyfronet.s4e.sync.step.LoadProduct;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Context implements BaseContext {
    private String initiatedByMethod;
    @EqualsAndHashCode.Include private String sceneKey;
    private String eventName;
    private LocalDateTime receivedAt;

    private final SceneJsonFileContext scene = new SceneJsonFileContext();
    private final JsonFileContext metadata = new JsonFileContext();
    private LoadProduct.ProductProjection product;
    private Prototype.PrototypeBuilder prototype;
    private final Error.ErrorBuilder error;

    private Long sceneId;

    public Context(String sceneKey) {
        this.sceneKey = sceneKey;
        error = Error.builder(sceneKey);
        scene.setKey(sceneKey);
    }
}
