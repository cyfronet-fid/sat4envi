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

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

public class QueryEnding extends QueryDecorator {
    private final int maxPageSize;
    private final int defaultPageSize;
    private final int defaultOffset;

    public QueryEnding(QueryBuilder queryBuilder, int maxPageSize, int defaultPageSize, int defaultOffset) {
        super(queryBuilder);
        this.maxPageSize = maxPageSize;
        this.defaultPageSize = defaultPageSize;
        this.defaultOffset = defaultOffset;
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery,
                                            Errors errors) {
        resultQuery.append(" ORDER BY ").append(getOrderByField(params)).append(" ").append(getOrder(params));
        resultQuery.append(" LIMIT ?");
        parameters.add(getLimit(params, errors));
        resultQuery.append(" OFFSET ?;");
        parameters.add(getOffset(params, errors));
    }

    @Override
    protected void doPrepareCountQueryAndParameters(Map<String, Object> params, List<Object> parameters, StringBuilder resultQuery, Errors errors) {
        resultQuery.append(";");
    }

    private String getOrderByField(Map<String, Object> params) {
        switch (String.valueOf(params.getOrDefault(SORT_BY, "id"))) {
            case "ingestionTime":
                return "f_cast_isots(metadata_content->>'ingestion_time')";
            case "sensingTime":
                return "f_cast_isots(metadata_content->>'sensing_time')";
            default:
                return "id";
        }
    }

    private String getOrder(Map<String, Object> params) {
        String param = String.valueOf(params.getOrDefault(ORDER, "DESC"));
        return param.equalsIgnoreCase("ASC") ? "ASC" : "DESC";
    }

    private int getLimit(Map<String, Object> params,
                         Errors errors) {
        try {
            int result = Integer.parseInt(params.getOrDefault(LIMIT, defaultPageSize).toString());
            if (result < 0) {
                throw new NumberFormatException("Limit cannot be negative");
            }
            return Math.min(result, maxPageSize);
        } catch (NumberFormatException e) {
            errors.rejectValue(LIMIT, "pl.cyfronet.s4e.data.repository.query.QueryEnding.limit.message", e.getMessage());
            return defaultPageSize;
        }
    }

    private int getOffset(Map<String, Object> params,
                          Errors errors) {
        try {
            int result = Integer.parseInt(params.getOrDefault(OFFSET, defaultOffset).toString());
            if (result < 0) {
                throw new NumberFormatException("Offset cannot be negative");
            }
            return result;
        } catch (NumberFormatException e) {
            errors.rejectValue(OFFSET, "pl.cyfronet.s4e.data.repository.query.QueryEnding.offset.message", e.getMessage());
            return defaultOffset;
        }
    }
}
