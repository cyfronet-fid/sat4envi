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

package pl.cyfronet.s4e.search;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;
import pl.cyfronet.s4e.license.LicensePermissionEvaluator;
import pl.cyfronet.s4e.search.SentinelSearchConfig.*;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.ArrayList;
import java.util.List;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

@Component
@RequiredArgsConstructor
public class SentinelSearchConfigSupplier {
    private interface ProductProjection extends ProjectionWithId {
        String getName();
    }

    private final ProductRepository productRepository;
    private final LicensePermissionEvaluator licensePermissionEvaluator;

    public SentinelSearchConfig getConfig(AppUserDetails userDetails) {
        val sentinel1Names = new ArrayList<String>();
        val sentinel2Names = new ArrayList<String>();
        val otherNames = new ArrayList<String>();

        productRepository.findAllBy(Sort.by("rank"), ProductProjection.class).stream()
            .filter(product -> licensePermissionEvaluator.allowProductRead(product.getId(), userDetails))
            .map(ProductProjection::getName)
            .forEach(productName -> {
                if (productName.startsWith("Sentinel-1-")) {
                    sentinel1Names.add(productName);
                } else if (productName.startsWith("Sentinel-2-")) {
                    sentinel2Names.add(productName);
                } else {
                    otherNames.add(productName);
                }
            });

        return new SentinelSearchConfig(
                new Common(List.of(
                        new SelectParam(SORT_BY, List.of("sensingTime", "ingestionTime", "id")),
                        new SelectParam(ORDER, List.of("DESC", "ASC")),
                        new DatetimeParam(SENSING_FROM),
                        new DatetimeParam(SENSING_TO)
                )),
                List.of(
                        new Section("sentinel-1", List.of(
                                new DatetimeParam(INGESTION_FROM),
                                new DatetimeParam(INGESTION_TO),
                                new SelectParam(PRODUCT_TYPE, sentinel1Names),
                                new SelectParam(SATELLITE_PLATFORM, List.of("", "Sentinel-1A", "Sentinel-1B")),
                                new SelectParam(PROCESSING_LEVEL, List.of("", "1", "2")),
                                new SelectParam(POLARISATION, List.of("", "HH", "VV", "HV", "VH", "HH+HV", "VV+VH")),
                                new SelectParam(SENSOR_MODE, List.of("", "SM", "IW", "EW", "WV")),
                                new TextParam(RELATIVE_ORBIT_NUMBER)
                        )),
                        new Section("sentinel-2", List.of(
                                new DatetimeParam(INGESTION_FROM),
                                new DatetimeParam(INGESTION_TO),
                                new SelectParam(PRODUCT_TYPE, sentinel2Names),
                                new SelectParam(SATELLITE_PLATFORM, List.of("", "Sentinel-2A", "Sentinel-2B")),
                                new SelectParam(PROCESSING_LEVEL, List.of("", "Level-1C", "Level-2A")),
                                new TextParam(RELATIVE_ORBIT_NUMBER),
                                new FloatParam(CLOUD_COVER, 0., 100.)
                        )),
                        new Section("other", List.of(
                                new SelectParam(PRODUCT_TYPE, otherNames)
                        ))
                )
        );
    }
}
