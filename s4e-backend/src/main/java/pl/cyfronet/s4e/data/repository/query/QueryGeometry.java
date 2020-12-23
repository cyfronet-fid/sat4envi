/*
 * Copyright 2021 ACC Cyfronet AGH
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
import pl.cyfronet.s4e.util.GeometryUtil;

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.api.SearchApiParams.FOOTPRINT;

public class QueryGeometry extends QueryDecorator {
    public QueryGeometry(QueryBuilder queryBuilder) {
        super(queryBuilder);
    }

    @Override
    protected void doPrepareQueryAndParameters(Map<String, Object> params,
                                               List<Object> parameters,
                                               StringBuilder resultQuery,
                                               Errors errors) {
        queryByPolygon(params, parameters, resultQuery);
    }

    private void queryByPolygon(Map<String, Object> params,
                                List<Object> parameters,
                                StringBuilder resultQuery) {
        if (params.containsKey(FOOTPRINT)) {

            resultQuery
                    .append(" AND ST_Intersects(footprint, ST_Transform(ST_GeomFromText(?, ")
                    .append(GeometryUtil.FACTORY_4326.getSRID())
                    .append("), ")
                    .append(GeometryUtil.FACTORY_3857.getSRID())
                    .append("))");
            parameters.add(String.valueOf(params.get(FOOTPRINT)));
        }
    }
}
