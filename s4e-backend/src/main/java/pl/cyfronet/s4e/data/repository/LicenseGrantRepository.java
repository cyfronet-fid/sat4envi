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

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.LicenseGrant;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface LicenseGrantRepository extends JpaRepository<LicenseGrant, Long> {
    @Query("SELECT lg FROM LicenseGrant lg WHERE lg.id = :id")
    @EntityGraph(attributePaths = { "institution", "product" })
    <T> Optional<T> findByIdFetchInstitutionAndProduct(Long id, Class<T> projection);

    Optional<LicenseGrant> findByProductIdAndInstitutionSlug(Long productId, String institutionSlug);

    boolean existsByProductIdAndInstitutionSlug(Long productId, String institutionSlug);

    @Query("SELECT lg FROM LicenseGrant lg")
    @EntityGraph(attributePaths = { "institution", "product" })
    <T> List<T> findAllFetchInstitutionAndProduct(Class<T> projection);

    @Query("SELECT lg FROM LicenseGrant lg WHERE lg.product.id = :productId")
    @EntityGraph(attributePaths = { "institution", "product" })
    <T> List<T> findAllByProductIdFetchInstitutionAndProduct(Long productId, Class<T> projection);

    @Query("SELECT lg FROM LicenseGrant lg WHERE lg.institution.slug = :institutionSlug")
    @EntityGraph(attributePaths = { "institution", "product" })
    <T> List<T> findAllByInstitutionSlugFetchInstitutionAndProduct(String institutionSlug, Class<T> projection);
}
