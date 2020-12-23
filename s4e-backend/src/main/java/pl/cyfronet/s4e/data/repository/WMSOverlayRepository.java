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
import pl.cyfronet.s4e.bean.OverlayOwner;
import pl.cyfronet.s4e.bean.WMSOverlay;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface WMSOverlayRepository extends CrudRepository<WMSOverlay, Long> {
    <T> Optional<T> findByIdAndAppUserIdAndOwnerType(
            Long id,
            Long appUserId,
            OverlayOwner ownerType,
            Class<T> projection
    );
    <T> Optional<T> findByIdAndOwnerTypeAndInstitutionId(
            Long id,
            OverlayOwner ownerType,
            Long institutionId,
            Class<T> projection
    );
    void deleteByIdAndOwnerType(Long id, OverlayOwner owner);

    List<WMSOverlay> findAllByOwnerType(OverlayOwner ownerType);

    @Query("SELECT w " +
            "FROM WMSOverlay w " +
            "LEFT JOIN FETCH w.institution i " +
            "LEFT JOIN FETCH i.membersRoles r " +
            "LEFT JOIN FETCH r.user u " +
            "WHERE w.ownerType = :ownerType AND u.id = :appUserId AND r.role= :role")
    List<WMSOverlay> findAllInstitutional(Long appUserId, AppRole role, OverlayOwner ownerType);

    @Query("SELECT w " +
            "FROM WMSOverlay w " +
            "WHERE w.appUser.id = :appUserId AND w.ownerType = :ownerType")
    List<WMSOverlay> findAllPersonal(Long appUserId, OverlayOwner ownerType);
}
