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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.Institution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface InstitutionRepository extends CrudRepository<Institution, Long> {
    <T> Optional<T> findById(Long id, Class<T> projection);

    boolean existsBySlug(String institutionSlug);

    <T> List<T> findAllBy(Class<T> projection);

    Optional<Institution> findBySlug(String slug);

    <T> Optional<T> findBySlug(String slug, Class<T> projection);

    Set<Institution> findAllByParentId(Long ParentId);

    @Query(value = "SELECT i.* " +
            "FROM Institution i " +
            "LEFT JOIN user_role r ON r.institution_id = i.id " +
            "LEFT JOIN app_user u ON u.id = r.app_user_id " +
            "WHERE u.email = :email " +
            "AND r.role in (:roles)", nativeQuery = true)
    <T> List<T> findInstitutionByUserEmailAndRoles(String email, List<String> roles, Class<T> projection);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.institution i " +
            "WHERE i.slug = :institutionSlug")
    <T> Set<T> findAllMembers(String institutionSlug, Class<T> projection);

    @Query("SELECT u.email " +
            "FROM UserRole r " +
            "JOIN r.user u " +
            "JOIN r.institution i " +
            "WHERE i.slug = :institutionSlug AND r.role = :role")
    Set<String> findAllMembersEmails(String institutionSlug, AppRole role);

    @Query("SELECT CASE WHEN COUNT(u)> 0 THEN 'true' ELSE 'false' END " +
            "FROM AppUser u " +
            "LEFT JOIN u.roles r " +
            "LEFT JOIN r.institution i " +
            "WHERE i.slug = :institutionSlug AND u.email = :email")
    boolean isMemberBySlugAndEmail(String institutionSlug, String email);

    @Transactional
    @Modifying
    void deleteInstitutionBySlug(String slug);

    @Transactional
    @Modifying
    @Query("delete from Institution")
    void deleteAll();
}
