package pl.cyfronet.s4e.sync.step.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.mockito.Mock;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.BaseStepTest;
import pl.cyfronet.s4e.util.GeometryUtil;

import javax.json.JsonObject;
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
