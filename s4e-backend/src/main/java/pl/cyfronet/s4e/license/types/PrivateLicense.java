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

package pl.cyfronet.s4e.license.types;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.security.AppUserDetails;

import static pl.cyfronet.s4e.security.AppUserDetailsUtil.isAdmin;
import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_READ_AUTHORITY_PREFIX;
import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_WRITE_AUTHORITY_PREFIX;

public class PrivateLicense implements WritableLicense {
    @Override
    public boolean canWrite(Product product, AppUserDetails userDetails) {
        if (isAdmin(userDetails)) {
            return true;
        }

        return hasWriteLicense(userDetails, product);
    }

    @Override
    public boolean canRead(Product product, AppUserDetails userDetails) {
        if (isAdmin(userDetails)) {
            return true;
        }

        return hasReadLicense(userDetails, product);
    }

    private boolean hasReadLicense(AppUserDetails userDetails, Product product) {
        return userDetails != null && userDetails.getAuthorities()
                .contains(new SimpleGrantedAuthority(LICENSE_READ_AUTHORITY_PREFIX + product.getId().toString()));
    }

    private boolean hasWriteLicense(AppUserDetails userDetails, Product product) {
        return userDetails != null && userDetails.getAuthorities()
                .contains(new SimpleGrantedAuthority(LICENSE_WRITE_AUTHORITY_PREFIX + product.getId().toString()));
    }
}
