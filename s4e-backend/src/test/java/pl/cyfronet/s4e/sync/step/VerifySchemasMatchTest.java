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
