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
