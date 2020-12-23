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

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Property;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PropertyRepository extends CrudRepository<Property, String> {
    Optional<Property> findByName(String name);

    <T> Optional<T> findByName(String name, Class<T> projection);

    <T> List<T> findAllBy(Class<T> projection);

    @Transactional
    void deleteByName(String name);
}
