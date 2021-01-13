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
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;
import pl.cyfronet.s4e.license.LicensePermissionEvaluator;
import pl.cyfronet.s4e.search.SearchPanelConfigResponse.*;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

@Component
@RequiredArgsConstructor
public class SearchPanelConfigResponseSupplier {
    private interface SchemaProjection extends ProjectionWithId {
        String getName();
    }

    private interface ProductProjection extends ProjectionWithId {
        String getName();
        SchemaProjection getMetadataSchema();
    }

    private final ProductRepository productRepository;
    private final LicensePermissionEvaluator licensePermissionEvaluator;
    private final List<SectionPrototype> searchPanelSectionPrototypes;

    public SearchPanelConfigResponse getConfig(AppUserDetails userDetails) {
        val schemaToProductNames = new HashMap<String, List<String>>();

        productRepository.findAllBy(Sort.by("rank"), ProductProjection.class).stream()
                .filter(product -> licensePermissionEvaluator.allowProductRead(product.getId(), userDetails))
                .forEach(product -> schemaToProductNames
                        .computeIfAbsent(product.getMetadataSchema().getName(), (key) -> new ArrayList<>())
                        .add(product.getName())
                );

        val sections = new ArrayList<Section>();
        for (val sectionConfig : searchPanelSectionPrototypes) {
            List<String> names = schemaToProductNames.remove(sectionConfig.getMetadataSchemaName());
            if (names != null && !names.isEmpty()) {
                List<Param> params = new ArrayList<>(sectionConfig.getPrefixParams());
                params.add(new SelectParam(PRODUCT_TYPE, names));
                params.addAll(sectionConfig.getSuffixParams());

                sections.add(new Section(sectionConfig.getName(), params));
            }
        }

        schemaToProductNames.entrySet().stream()
                .sorted(Entry.comparingByKey())
                .forEach(entry -> {
                    List<Param> params = new ArrayList<>();
                    params.add(new SelectParam(PRODUCT_TYPE, entry.getValue()));
                    sections.add(new Section(entry.getKey(), params));
                });

        return new SearchPanelConfigResponse(
                new Common(List.of(
                        new SelectParam(SORT_BY, List.of("sensingTime", "ingestionTime", "id")),
                        new SelectParam(ORDER, List.of("DESC", "ASC")),
                        new DatetimeParam(SENSING_FROM),
                        new DatetimeParam(SENSING_TO)
                )),
                sections
        );
    }
}
