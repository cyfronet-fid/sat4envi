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

package pl.cyfronet.s4e.data.repository.query;

import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.PRODUCT_TYPE;

public class QueryProductType extends QueryDecorator {
    public QueryProductType(QueryBuilder queryBuilder) {
        super(queryBuilder);
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery,
                                            Errors errors) {
        Object paramValue = params.get(PRODUCT_TYPE);
        if (paramValue instanceof String) {
            resultQuery.append(" AND product.name = ?");
            parameters.add(paramValue);
        }
    }
}
