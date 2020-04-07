package pl.cyfronet.s4e.sync.step.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.BaseStepTest;

import javax.json.JsonObject;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.step.metadata.IngestTimestamp.METADATA_TIMESTAMP_PROPERTY;

class IngestTimestampTest extends BaseStepTest<BaseContext> {
    @Mock
    private JsonObject metadataJson;

    @Mock
    private BiConsumer<BaseContext, LocalDateTime> update;

    private IngestTimestamp step;

    @BeforeEach
    public void beforeEach() {
        step = IngestTimestamp.builder()
                .metadataJson(c -> metadataJson)
                .update(update)
                .build();
    }

    @Test
    public void shouldWork() {
        when(metadataJson.getString(METADATA_TIMESTAMP_PROPERTY))
                .thenReturn("2019-09-09T10:11:12.000000+00:00");

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(context, LocalDateTime.of(2019, 9, 9, 10, 11, 12));
        verifyNoMoreInteractions(update);
    }

}
