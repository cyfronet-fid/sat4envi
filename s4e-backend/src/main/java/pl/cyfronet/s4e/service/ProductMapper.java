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

package pl.cyfronet.s4e.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.config.MapStructCentralConfig;

@Mapper(
        config = MapStructCentralConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licenseGrants", ignore = true)
    @Mapping(target = "sceneSchema", ignore = true)
    @Mapping(target = "metadataSchema", ignore = true)
    @Mapping(target = "scenes", ignore = true)
    @Mapping(target = "favourites", ignore = true)
    @Mapping(target = "productCategory", ignore = true)
    void create(ProductService.DTO dto, @MappingTarget Product.ProductBuilder productBuilder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "licenseGrants", ignore = true)
    @Mapping(target = "sceneSchema", ignore = true)
    @Mapping(target = "metadataSchema", ignore = true)
    @Mapping(target = "scenes", ignore = true)
    @Mapping(target = "favourites", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "productCategory", ignore = true)
    void update(ProductService.DTO dto, @MappingTarget Product product);
}
