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

package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.api.MappedScene;
import pl.cyfronet.s4e.api.SearchConverter;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.QueryException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SceneRepository repository;
    private final SearchConverter converter;

    public List<MappedScene> getScenesBy(Map<String, Object> params) throws SQLException, QueryException {
        return repository.findAllByParamsMap(params);
    }

    public Long getCountBy(Map<String, Object> params) throws SQLException, QueryException {
        return repository.countAllByParamsMap(params);
    }

    public Map<String, Object> countParseToParamMap(String query) {
        Map<String, Object> result = new HashMap<>();
        if (query != null) {
            result.putAll(converter.convert(query));
        }
        return result;
    }

    public Map<String, Object> parseToParamMap(String rowsSize, String rowStart, String orderby, String query) {
        Map<String, Object> result = new HashMap<>();
        if (query != null) {
            result.putAll(converter.convert(query));
        }
        result.putAll(converter.convertParams(rowsSize, rowStart, orderby));
        return result;
    }
}
