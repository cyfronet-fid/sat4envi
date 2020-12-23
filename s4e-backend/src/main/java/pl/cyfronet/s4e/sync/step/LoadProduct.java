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

package pl.cyfronet.s4e.sync.step;

import jakarta.json.JsonObject;
import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.sync.Error.ERR_PRODUCT_NOT_FOUND;

@Builder
public class LoadProduct<T extends BaseContext> implements Step<T, Error> {
    public static final String SCENE_PRODUCT_TYPE_PROPERTY = "product_type";

    public interface IdAndNameProjection {
        Long getId();

        String getName();
    }

    public interface ProductProjection {
        Long getId();

        String getName();

        IdAndNameProjection getSceneSchema();

        IdAndNameProjection getMetadataSchema();

        Map<String, String> getGranuleArtifactRule();
    }

    private final Supplier<ProductRepository> productRepository;

    private final Function<T, JsonObject> json;
    private final BiConsumer<T, ProductProjection> update;

    @Override
    public Error apply(T context) {
        val error = context.getError();

        ProductRepository productRepository = this.productRepository.get();

        JsonObject json = this.json.apply(context);

        String productType = json.getString(SCENE_PRODUCT_TYPE_PROPERTY);
        val product = productRepository.findByName(productType, ProductProjection.class).orElse(null);
        if (product == null) {
            return error.code(ERR_PRODUCT_NOT_FOUND)
                    .parameter("product_type", productType).build();
        }
        update.accept(context, product);
        return null;
    }
}
