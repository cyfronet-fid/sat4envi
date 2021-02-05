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

package pl.cyfronet.s4e;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.data.repository.SyncRecordRepository;

@Service
@RequiredArgsConstructor
public class TestDbHelper {
    private final InstitutionRepository institutionRepository;
    private final AppUserRepository appUserRepository;
    private final PlaceRepository placeRepository;
    private final ProductRepository productRepository;
    private final SchemaRepository schemaRepository;
    private final WMSOverlayRepository wmsOverlayRepository;
    private final PRGOverlayRepository prgOverlayRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PropertyRepository propertyRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final SyncRecordRepository syncRecordRepository;

    public void clean() {
        productCategoryRepository.deleteAllByNameNot(ProductCategoryRepository.DEFAULT_CATEGORY_NAME);
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();
        placeRepository.deleteAll();
        productRepository.deleteAll();
        schemaRepository.deleteAll();
        wmsOverlayRepository.deleteAll();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();
        propertyRepository.deleteAll();
        syncRecordRepository.deleteAll();
    }
}
