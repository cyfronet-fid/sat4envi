/*
 * Copyright 2022 ACC Cyfronet AGH
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

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface ProductRepository extends CrudRepository<Product, Long> {
    <T> List<T> findAllBy(Sort sort, Class<T> projection);

    List<Product> findAllByDownloadOnlyFalse();

    @Query("SELECT p FROM Product p")
    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema", "productCategory"})
    <T> List<T> findAllFetchSchemasAndCategory(Sort sort, Class<T> projection);

    @Query("SELECT p FROM Product p WHERE p.downloadOnly = FALSE")
    @EntityGraph(attributePaths = {"productCategory"})
    <T> List<T> findAllByDownloadOnlyFalseFetchProductCategory(Sort sort, Class<T> projection);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    @EntityGraph(attributePaths = {"productCategory"})
    <T> List<T> findAllByIdInFetchProductCategory(Set<Long> ids, Sort sort, Class<T> projection);

    <T> Optional<T> findById(Long id, Class<T> projection);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @EntityGraph(attributePaths = {"productCategory"})
    <T> Optional<T> findByIdFetchCategory(Long id, Class<T> projection);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema", "productCategory"})
    <T> Optional<T> findByIdFetchSchemasAndCategory(Long id, Class<T> projection);

    Optional<Product> findByName(String name);

    @EntityGraph(attributePaths = {"sceneSchema", "metadataSchema", "productCategory"})
    <T> Optional<T> findByName(String name, Class<T> projection);

    boolean existsBySceneSchemaId(Long id);

    boolean existsByMetadataSchemaId(Long id);

    @Query("SELECT CASE WHEN COUNT(p)> 0 THEN 'true' ELSE 'false' END " +
            "FROM Product p " +
            "JOIN p.favourites f " +
            "WHERE p.id = :productId AND f.email = :email")
    boolean isFavouriteByEmailAndProductId(String email, Long productId);
}
