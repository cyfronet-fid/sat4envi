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
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.controller.AppUserController;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.controller.response.UserMeResponse;
import pl.cyfronet.s4e.security.AppUserDetails;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class AppUserMapper {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", qualifiedByName = "password")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    public abstract AppUser requestToPreEntity(RegisterRequest registerRequest);

    @Named("password")
    protected String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }


    @Mapping(target = "id", source = "projection.id")
    @Mapping(target = "email", source = "projection.email")
    @Mapping(target = "name", source = "projection.name")
    @Mapping(target = "surname", source = "projection.surname")
    @Mapping(target = "admin", source = "userDetails", qualifiedByName = "admin")
    @Mapping(target = "memberZK", source = "userDetails", qualifiedByName = "memberZK")
    @Mapping(target = "authorities", source = "userDetails.authorities")
    public abstract UserMeResponse projectionToMeResponse(
            AppUserController.UserMeProjection projection,
            AppUserDetails userDetails
    );

    protected String sgaToString(SimpleGrantedAuthority simpleGrantedAuthority) {
        return simpleGrantedAuthority.getAuthority();
    }

    @Named("admin")
    protected boolean admin(AppUserDetails userDetails) {
        return containsAuthority(userDetails, "ROLE_ADMIN");
    }

    @Named("memberZK")
    protected boolean memberZK(AppUserDetails userDetails) {
        return containsAuthority(userDetails, "ROLE_MEMBER_ZK");
    }

    private boolean containsAuthority(AppUserDetails userDetails, String authority) {
        return userDetails.getAuthorities().contains(new SimpleGrantedAuthority(authority));
    }
}
