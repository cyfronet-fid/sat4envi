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

import jakarta.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.mockito.Mock;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.BaseStepTest;
import pl.cyfronet.s4e.util.GeometryUtil;

import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.step.metadata.IngestFootprint.METADATA_FOOTPRINT_PROPERTY;

class IngestFootprintTest extends BaseStepTest<BaseContext> {
    private GeometryUtil geometryUtil;

    @Mock
    private JsonObject metadataJson;

    @Mock
    private BiConsumer<BaseContext, Geometry> update;

    private IngestFootprint step;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        geometryUtil = new GeometryUtil();

        step = IngestFootprint.builder()
                .geometryUtil(() -> geometryUtil)
                .metadataJson(c -> metadataJson)
                .update(update)
                .build();
    }

    @Test
    public void shouldWork() {
        when(metadataJson.getString(METADATA_FOOTPRINT_PROPERTY))
                .thenReturn("0,0 0,1 1,1 1,0");

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(eq(context), any(Geometry.class));
        verifyNoMoreInteractions(update);
    }
}
