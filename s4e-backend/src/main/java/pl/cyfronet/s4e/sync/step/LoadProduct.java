package pl.cyfronet.s4e.sync.step;

import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import javax.json.JsonObject;
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
