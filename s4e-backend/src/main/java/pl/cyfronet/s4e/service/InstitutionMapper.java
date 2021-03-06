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

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class InstitutionMapper {
    @Autowired
    private SlugService slugService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", source = "name", qualifiedByName = "slug")
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "membersRoles", ignore = true)
    @Mapping(target = "licenseGrants", ignore = true)
    @Mapping(target = "eumetsatLicense", ignore = true)
    public abstract Institution requestToPreEntity(CreateInstitutionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", source = "name", qualifiedByName = "slug")
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "membersRoles", ignore = true)
    @Mapping(target = "licenseGrants", ignore = true)
    @Mapping(target = "eumetsatLicense", ignore = true)
    @Mapping(target = "zk", ignore = true)
    @Mapping(target = "pak", ignore = true)
    public abstract Institution requestToPreEntity(CreateChildInstitutionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", source = "name", qualifiedByName = "slug")
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "membersRoles", ignore = true)
    @Mapping(target = "licenseGrants", ignore = true)
    @Mapping(target = "eumetsatLicense", ignore = true)
    @Mapping(target = "zk", ignore = true)
    @Mapping(target = "pak", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    public abstract void update(UpdateInstitutionRequest request, @MappingTarget Institution institution);

    @Named("slug")
    protected String toSlug(String name) {
        return slugService.slugify(name);
    }
}
