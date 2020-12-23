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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.SavedView;
import pl.cyfronet.s4e.controller.response.SavedViewResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
public interface SavedViewRepository extends CrudRepository<SavedView, UUID> {
    <T> Optional<T> findById(UUID id, Class<T> projection);

    @Query("SELECT sv " +
            "FROM SavedView sv " +
            "JOIN sv.owner u " +
            "WHERE u.email = :email")
    Page<SavedViewResponse> findAllByOwnerEmail(String email, Pageable pageable);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "JOIN SavedView sv ON sv.owner.id = u.id " +
            "WHERE sv.id = :id")
    <T> Optional<T> findOwnerOf(UUID id, Class<T> projection);

    // Test
    <T> List<T> findAllBy(Class<T> projection);
}
