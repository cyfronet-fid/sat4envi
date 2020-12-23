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

import static pl.cyfronet.s4e.search.SearchQueryParams.CLOUD_COVER;

public class QueryNumber extends QueryDecorator {
    public static final int MAX_CLOUD_COVERAGE = 100;
    public static final int MIN_CLOUD_COVERAGE = 0;

    public QueryNumber(QueryBuilder queryBuilder) {
        super(queryBuilder);
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery,
                                            Errors errors) {
        queryByCloudCover(params, parameters, resultQuery, errors);
    }

    private void queryByCloudCover(Map<String, Object> params,
                                   List<Object> parameters,
                                   StringBuilder resultQuery,
                                   Errors errors) {
        if (params.containsKey(CLOUD_COVER)) {
            resultQuery.append(" AND (metadata_content ->> 'cloud_cover')::float <= ?");
            parameters.add(getCloudCover(params, errors));
        }
    }

    private float getCloudCover(Map<String, Object> params,
                                Errors errors) {
        try {
            float result = Float.parseFloat(params.get(CLOUD_COVER).toString());
            if (result < MIN_CLOUD_COVERAGE || result > MAX_CLOUD_COVERAGE) {
                throw new NumberFormatException("Cloud cover range is [0 - 100]");
            }
            return result;
        } catch (NumberFormatException e) {
            errors.rejectValue(CLOUD_COVER, "pl.cyfronet.s4e.data.repository.query.QueryNumber.message", e.getMessage());
            return 0;
        }
    }
}
