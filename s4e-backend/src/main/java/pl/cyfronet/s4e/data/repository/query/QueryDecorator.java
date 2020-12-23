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

import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class QueryDecorator implements QueryBuilder {
    private final QueryBuilder queryBuilder;

    protected abstract void doPrepareQueryAndParameters(Map<String, Object> params,
                                                        List<Object> parameters,
                                                        StringBuilder resultQuery,
                                                        Errors errors);

    protected void doPrepareCountQueryAndParameters(Map<String, Object> params,
                                                        List<Object> parameters,
                                                        StringBuilder resultQuery,
                                                        Errors errors){
        doPrepareQueryAndParameters(params, parameters, resultQuery, errors);
    }

    @Override
    public final void prepareQueryAndParameters(Map<String, Object> params,
                                                List<Object> parameters,
                                                StringBuilder resultQuery,
                                                Errors errors) {
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        doPrepareQueryAndParameters(params, parameters, resultQuery, errors);
    }

    @Override
    public final void prepareCountQueryAndParameters(Map<String, Object> params,
                                                List<Object> parameters,
                                                StringBuilder resultQuery,
                                                Errors errors) {
        queryBuilder.prepareCountQueryAndParameters(params, parameters, resultQuery, errors);
        doPrepareCountQueryAndParameters(params, parameters, resultQuery, errors);
    }
}
