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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.Optional;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.Error.ERR_PRODUCT_NOT_FOUND;
import static pl.cyfronet.s4e.sync.step.LoadProduct.SCENE_PRODUCT_TYPE_PROPERTY;

class LoadProductTest extends BaseStepTest<BaseContext> {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private BiConsumer<BaseContext, LoadProduct.ProductProjection> update;

    @Mock
    private JsonObject json;

    private String productType = "prod-name";

    private LoadProduct step;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        when(context.getError()).thenReturn(Error.builder(sceneKey));

        step = LoadProduct.builder()
                .productRepository(() -> productRepository)
                .json(c -> json)
                .update(update)
                .build();

        when(json.getString(SCENE_PRODUCT_TYPE_PROPERTY)).thenReturn(productType);
    }

    @Test
    public void shouldWork() {
        LoadProduct.ProductProjection productProjection = mock(LoadProduct.ProductProjection.class);
        when(productRepository.findByName(productType, LoadProduct.ProductProjection.class))
                .thenReturn(Optional.of(productProjection));

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(context, productProjection);
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldHandleMissingProduct() {
        when(productRepository.findByName(any(), any())).thenReturn(Optional.empty());

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_PRODUCT_NOT_FOUND)));
        assertThat(error.getParameters(), hasEntry("product_type", productType));
        verifyNoMoreInteractions(update);
    }
}
