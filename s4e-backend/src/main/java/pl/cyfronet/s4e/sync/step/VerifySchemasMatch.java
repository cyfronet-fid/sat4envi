package pl.cyfronet.s4e.sync.step;

import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.function.Function;

import static pl.cyfronet.s4e.sync.Error.ERR_SCHEMA_PRODUCT_MISMATCH;

@Builder
public class VerifySchemasMatch<T extends BaseContext> implements Step<T, Error> {
    private final Function<T, String> productSchemaName;
    private final Function<T, String> fileSchemaName;

    @Override
    public Error apply(T context) {
        val error = context.getError();

        String productSchemaName = this.productSchemaName.apply(context);
        String fileSchemaName = this.fileSchemaName.apply(context);

        if (productSchemaName != null && !productSchemaName.equals(fileSchemaName)) {
            return error.code(ERR_SCHEMA_PRODUCT_MISMATCH)
                    .parameter("product_schema_name", productSchemaName)
                    .parameter("file_schema_name", fileSchemaName)
                    .build();
        }

        return null;
    }
}
