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

package pl.cyfronet.s4e.data.repository;

import pl.cyfronet.s4e.ex.QueryException;
import pl.cyfronet.s4e.api.MappedScene;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

//an interface with the methods you wish to use with EntityManger.
public interface SceneRepositoryExt {
    List<MappedScene> findAllByParamsMap(Map<String, Object> params) throws SQLException, QueryException;
    Long countAllByParamsMap(Map<String, Object> params) throws SQLException, QueryException;
}
