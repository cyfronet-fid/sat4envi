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
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.Prototype;
import pl.cyfronet.s4e.sync.ScenePersister;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.Step;

import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.sync.Error.ERR_PRODUCT_NOT_FOUND;

@Builder
public class Persist<T extends BaseContext> implements Step<T, Error> {
    private final Supplier<ScenePersister> scenePersister;

    private final Function<T, Prototype> prototype;

    @Override
    public Error apply(T context) {
        Error.ErrorBuilder error = context.getError();
        ScenePersister scenePersister = this.scenePersister.get();

        Prototype prototype = this.prototype.apply(context);
        try {
            scenePersister.persist(prototype);
        } catch (NotFoundException e) {
            return error.code(ERR_PRODUCT_NOT_FOUND).cause(e)
                    .parameter("prototype", prototype).build();
        }
        return null;
    }
}
