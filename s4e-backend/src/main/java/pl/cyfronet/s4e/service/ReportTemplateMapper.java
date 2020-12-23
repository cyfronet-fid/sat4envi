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
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.ReportTemplate;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class ReportTemplateMapper {
    @Autowired
    private AppUserRepository appUserRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "ownerEmail")
    public abstract ReportTemplate dtoToPreEntity(ReportTemplateService.CreateDTO createDTO) throws NotFoundException;

    protected AppUser ownerEmailToAppUser(String ownerEmail) throws NotFoundException {
        return appUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("AppUser with email '" + ownerEmail + "' not found"));
    }
}
