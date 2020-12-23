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

package pl.cyfronet.s4e.sync.step.metadata;

import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.Prototype;
import pl.cyfronet.s4e.sync.context.Context;
import pl.cyfronet.s4e.sync.step.Step;

import java.util.function.BiConsumer;

@Builder
public class Init implements Step<Context, pl.cyfronet.s4e.sync.Error> {
    private final BiConsumer<Context, Prototype.PrototypeBuilder> update;

    @Override
    public Error apply(Context context) {
        val prototype = Prototype.builder()
                .productId(context.getProduct().getId())
                .sceneKey(context.getScene().getKey())
                .sceneJson(context.getScene().getJson())
                .metadataJson(context.getMetadata().getJson());

        update.accept(context, prototype);
        return null;
    }
}
