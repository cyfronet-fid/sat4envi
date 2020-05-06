package pl.cyfronet.s4e.sync.step.metadata;

import lombok.Builder;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.LoadProduct;
import pl.cyfronet.s4e.sync.step.Step;

import javax.json.JsonObject;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Builder
public class IngestS3Path<T extends BaseContext> implements Step<T, Error> {
    public static final String METADATA_FORMAT_PROPERTY = "format";
    public static final String METADATA_FORMAT_DEFAULT = "default";

    private final Function<T, JsonObject> metadataJson;
    private final Function<T, LoadProduct.ProductProjection> product;
    private final Function<T, Map<String, String>> artifacts;
    private final BiConsumer<T, String> update;

    @Override
    public Error apply(T context) {
        JsonObject metadataJson = this.metadataJson.apply(context);
        LoadProduct.ProductProjection product = this.product.apply(context);
        Map<String, String> artifacts = this.artifacts.apply(context);

        String format = metadataJson.getString(METADATA_FORMAT_PROPERTY, METADATA_FORMAT_DEFAULT);
        Map<String, String> granuleArtifactRule = product.getGranuleArtifactRule();
        String artifactKey = getArtifactKey(format, granuleArtifactRule);
        String path = artifacts.get(artifactKey);

        update.accept(context, path);

        return null;
    }

    private String getArtifactKey(String format, Map<String, String> granuleArtifactRule) {
        String key = granuleArtifactRule.get(format);
        if (key != null) {
            return key;
        }
        return granuleArtifactRule.get(METADATA_FORMAT_DEFAULT);
    }
}
