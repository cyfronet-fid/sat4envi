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

package pl.cyfronet.s4e.config;

import lombok.val;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

public class InstitutionSecurityHelper {
    public boolean isAdmin(String institutionSlug) {
        AppUserDetails details = AppUserDetailsSupplier.get();
        if (details == null) {
            return false;
        }
        val authorities = details.getAuthorities();
        return authorities.contains(simpleGrantedAuthority(institutionSlug, AppRole.INST_ADMIN));
    }

    public boolean isMember(String institutionSlug) {
        AppUserDetails details = AppUserDetailsSupplier.get();
        if (details == null) {
            return false;
        }
        val authorities = details.getAuthorities();
        return authorities.contains(simpleGrantedAuthority(institutionSlug, AppRole.INST_MEMBER));
    }

    private SimpleGrantedAuthority simpleGrantedAuthority(String institutionSlug, AppRole appRole) {
        String role = String.join("_", "ROLE", appRole.name(), institutionSlug);
        return new SimpleGrantedAuthority(role);
    }
}
