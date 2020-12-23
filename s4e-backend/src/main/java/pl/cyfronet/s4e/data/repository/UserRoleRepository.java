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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.UserRole;

import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    Optional<UserRole> findByUser_IdAndInstitution_IdAndRole(Long userId, Long institutionId, AppRole role);
    @Query(value = "SELECT r " +
            "FROM UserRole r " +
            "LEFT JOIN r.user u " +
            "LEFT JOIN r.institution i " +
            "WHERE i.slug = :institutionSlug AND u.email = :email")
    Set<UserRole> findUserRolesInInstitution(String email, String institutionSlug);
    @Query(value = "SELECT r " +
            "FROM UserRole r " +
            "LEFT JOIN r.user u " +
            "LEFT JOIN r.institution i " +
            "WHERE i.slug = :institutionSlug AND u.email = :email AND r.role = :role")
    Optional<UserRole>  findUserRolesInInstitution(String email, String institutionSlug, AppRole role);
    @Query(value = "SELECT r " +
            "FROM UserRole r " +
            "LEFT JOIN r.user u " +
            "LEFT JOIN r.institution i " +
            "WHERE u.id = :userId")
    Set<UserRole> findByUser_Id(Long userId);
    Set<UserRole> findByUser_IdAndInstitution_Id(Long userId, Long institutionId);

}
