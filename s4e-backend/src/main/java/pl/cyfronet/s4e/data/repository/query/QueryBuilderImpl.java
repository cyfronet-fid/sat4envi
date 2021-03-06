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

import lombok.val;
import org.springframework.validation.Errors;
import pl.cyfronet.s4e.bean.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryBuilderImpl implements QueryBuilder {
    private static final String SELECT_QUERY_PREFIX;
    private static final String COUNT_QUERY_PREFIX = "SELECT COUNT(*) FROM scene JOIN product ON scene.product_id = product.id WHERE true ";
    static {
        val columns = new ArrayList<String>();
        columns.add("scene." + Scene.COLUMN_ID);
        columns.add(Scene.COLUMN_PRODUCT_ID);
        columns.add(Scene.COLUMN_SCENE_KEY);
        columns.add("ST_AsText(ST_Transform(" + Scene.COLUMN_FOOTPRINT + ",4326),5) AS footprint");
        columns.add(Scene.COLUMN_METADATA);
        columns.add(Scene.COLUMN_CONTENT);
        columns.add(Scene.COLUMN_TIMESTAMP);
        SELECT_QUERY_PREFIX =
                "SELECT " +
                String.join(", ", columns) +
                " FROM scene JOIN product ON scene.product_id = product.id WHERE true";
    }

    @Override
    public void prepareQueryAndParameters(
            Map<String, Object> params,
            List<Object> parameters,
            StringBuilder resultQuery,
            Errors errors
    ) {
        resultQuery.append(SELECT_QUERY_PREFIX);
    }

    @Override
    public void prepareCountQueryAndParameters(
            Map<String, Object> params,
            List<Object> parameters,
            StringBuilder resultQuery,
            Errors errors
    ) {
        resultQuery.append(COUNT_QUERY_PREFIX);
    }
}
