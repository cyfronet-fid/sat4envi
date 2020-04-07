package pl.cyfronet.s4e.sync.step.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.BaseStepTest;
import pl.cyfronet.s4e.sync.step.LoadProduct;

import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.step.metadata.IngestS3Path.METADATA_FORMAT_PROPERTY;

class IngestS3PathTest extends BaseStepTest<BaseContext> {
    @Mock
    private JsonObject metadataJson;

    @Mock
    private LoadProduct.ProductProjection product;

    @Mock
    private BiConsumer<BaseContext, String> update;

    private IngestS3Path step;

    private Map<String, String> artifacts;

    @BeforeEach
    public void beforeEach() {
        artifacts = new HashMap<>();

        step = IngestS3Path.builder()
                .metadataJson(c -> metadataJson)
                .product(c -> product)
                .artifacts(c -> artifacts)
                .update(update)
                .build();
    }

    @Test
    public void shouldWork() {
        when(metadataJson.getString(eq(METADATA_FORMAT_PROPERTY), any(String.class)))
                .thenReturn("GeoTiff");
        artifacts.put("key_1", "/some/path");
        when(product.getGranuleArtifactRule()).thenReturn(Map.of("GeoTiff", "key_1"));

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(context, "some/path");
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldUseDefault() {
        when(metadataJson.getString(eq(METADATA_FORMAT_PROPERTY), any(String.class)))
                .thenReturn("GeoTiff");
        artifacts.put("key_1", "/some/path");
        when(product.getGranuleArtifactRule()).thenReturn(Map.of("default", "key_1"));

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(context, "some/path");
        verifyNoMoreInteractions(update);
    }

}
