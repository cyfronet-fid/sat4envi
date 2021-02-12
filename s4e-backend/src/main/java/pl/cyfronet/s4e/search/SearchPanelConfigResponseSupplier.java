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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

@Component
@RequiredArgsConstructor
public class SearchPanelConfigResponseSupplier {
    private interface SchemaProjection extends ProjectionWithId {
        String getName();
    }

    private interface ProductProjection extends ProjectionWithId {
        String getName();
        String getDisplayName();
        SchemaProjection getMetadataSchema();
    }

    private final ProductRepository productRepository;
    private final LicensePermissionEvaluator licensePermissionEvaluator;
    private final List<SectionPrototype> searchPanelSectionPrototypes;
    private final MessageSource messageSource;

    public SearchPanelConfigResponse getConfig(AppUserDetails userDetails) {
        val schemaToProductNames = new HashMap<String, List<SelectValue>>();

        productRepository.findAllBy(Sort.by("rank"), ProductProjection.class).stream()
                .filter(product -> licensePermissionEvaluator.allowProductRead(product.getId(), userDetails))
                .forEach(product -> schemaToProductNames
                        .computeIfAbsent(product.getMetadataSchema().getName(), (key) -> new ArrayList<>())
                        .add(new SelectValue(product.getName(), product.getDisplayName()))
                );

        val sections = new ArrayList<Section>();
        for (val sectionConfig : searchPanelSectionPrototypes) {
            List<SelectValue> selectValues = schemaToProductNames.remove(sectionConfig.getMetadataSchemaName());
            if (selectValues != null && !selectValues.isEmpty()) {
                List<Param> params = new ArrayList<>();
                sectionConfig.getPrefixParams().stream().map(this::mapToParam).forEach(params::add);
                params.add(selectParam(PRODUCT_TYPE, selectValues));
                sectionConfig.getSuffixParams().stream().map(this::mapToParam).forEach(params::add);

                sections.add(new Section(sectionConfig.getMetadataSchemaName(), sectionConfig.getLabel(), params));
            }
        }

        schemaToProductNames.entrySet().stream()
                .sorted(Entry.comparingByKey())
                .forEach(entry -> {
                    List<Param> params = new ArrayList<>();
                    params.add(selectParam(PRODUCT_TYPE, entry.getValue()));
                    sections.add(new Section(entry.getKey(), sectionLabelFromSchemaName(entry.getKey()), params));
                });

        return new SearchPanelConfigResponse(
                new Common(List.of(
                        selectParam(SORT_BY, selectValues(SORT_BY, List.of("sensingTime", "ingestionTime", "id"))),
                        selectParam(ORDER, selectValues(ORDER, List.of("DESC", "ASC"))),
                        new DatetimeParam(SENSING_FROM, translate(SENSING_FROM)),
                        new DatetimeParam(SENSING_TO, translate(SENSING_TO))
                )),
                sections
        );
    }

    private Param mapToParam(ParamPrototype prototype) {
        String queryParam = prototype.getQueryParam();
        if (prototype instanceof SelectParamPrototype) {
            // Prototype selects have non-translatable values so don't attempt to translate them.
            List<SelectValue> selectValues = ((SelectParamPrototype) prototype).getValues().stream()
                    .map(value -> new SelectValue(value, value))
                    .collect(Collectors.toUnmodifiableList());
            return selectParam(queryParam, selectValues);
        } else if (prototype instanceof FloatParamPrototype) {
            FloatParamPrototype floatParamPrototype = (FloatParamPrototype) prototype;
            return new FloatParam(
                    queryParam,
                    translate(queryParam),
                    floatParamPrototype.getMin(),
                    floatParamPrototype.getMax()
            );
        } else if (prototype instanceof TextParamPrototype) {
            return new TextParam(queryParam, translate(queryParam));
        } else if (prototype instanceof DatetimeParamPrototype) {
            return new DatetimeParam(queryParam, translate(queryParam));
        } else {
            throw new IllegalStateException();
        }
    }

    private SelectParam selectParam(String queryParam, List<SelectValue> values) {
        return new SelectParam(queryParam, translate(queryParam), values);
    }

    private List<SelectValue> selectValues(String queryParam, List<String> values) {
        return values.stream()
                .map(value -> {
                    String label = translate(queryParam + "." + value);
                    return new SelectValue(value, label);
                })
                .collect(Collectors.toUnmodifiableList());
    }

    private String translate(String path) {
        return messageSource.getMessage("search." + path, null, LocaleContextHolder.getLocale());
    }

    private String sectionLabelFromSchemaName(String schemaName) {
        if (schemaName == null) {
            return "";
        }
        String[] segments = schemaName.split("\\.");
        // A correct schema name is e.g. MSG_Raw.metadata.v1.json, so it has 4 segments, otherwise just return the same
        if (segments.length != 4) {
            return schemaName;
        }
        // Skip the metadata part and extension, and replace _ with spaces in the first part,
        // so the example becomes MSG Raw v1.
        return segments[0].replace('_', ' ') + " " + segments[2];
    }
}
