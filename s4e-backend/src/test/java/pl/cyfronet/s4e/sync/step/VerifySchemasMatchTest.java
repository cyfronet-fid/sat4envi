package pl.cyfronet.s4e.sync.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static pl.cyfronet.s4e.sync.Error.ERR_SCHEMA_PRODUCT_MISMATCH;

class VerifySchemasMatchTest extends BaseStepTest<BaseContext> {
    private VerifySchemasMatch step;

    private String fileSchemaName;

    private String productSchemaName;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        step = VerifySchemasMatch.builder()
                .fileSchemaName(c -> fileSchemaName)
                .productSchemaName(c -> productSchemaName)
                .build();
        fileSchemaName = "schema1";
        productSchemaName = fileSchemaName;
    }

    @Test
    public void shouldWork() {
        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
    }

    @Test
    public void shouldHandleSchemaMismatch() {
        productSchemaName = fileSchemaName + "different";

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_SCHEMA_PRODUCT_MISMATCH)));
        assertThat(error.getParameters(), hasEntry("product_schema_name", productSchemaName));
        assertThat(error.getParameters(), hasEntry("file_schema_name", fileSchemaName));
    }
}
