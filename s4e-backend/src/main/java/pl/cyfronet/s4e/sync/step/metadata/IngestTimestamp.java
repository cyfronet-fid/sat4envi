package pl.cyfronet.s4e.sync.step.metadata;

import lombok.Builder;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.Step;

import javax.json.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Builder
public class IngestTimestamp<T extends BaseContext> implements Step<T, Error> {
    public static final String METADATA_TIMESTAMP_PROPERTY = "sensing_time";
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final Function<T, JsonObject> metadataJson;
    private final BiConsumer<T, LocalDateTime> update;

    @Override
    public Error apply(T context) {
        JsonObject metadataJson = this.metadataJson.apply(context);

        String timestampString = metadataJson.getString(METADATA_TIMESTAMP_PROPERTY);
        LocalDateTime timestamp = LocalDateTime.parse(timestampString, TIMESTAMP_FORMATTER);

        update.accept(context, timestamp);

        return null;
    }
}
