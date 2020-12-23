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

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.time.*;

import static pl.cyfronet.s4e.security.AppUserDetailsUtil.isAdmin;

@RequiredArgsConstructor
public class EumetsatLicense implements TimestampGranularLicense {
    private final Clock clock;
    private final Duration freshDuration;

    @Override
    public boolean canRead(Product product, AppUserDetails userDetails) {
        return true;
    }

    @Override
    public boolean canRead(LocalDateTime timestamp, AppUserDetails userDetails) {
        if (isFreshAndNotOnTheHour(timestamp)) {
            if (isAdmin(userDetails)) {
                return true;
            }

            return hasLicense(userDetails);
        } else {
            return true;
        }
    }

    private boolean isFreshAndNotOnTheHour(LocalDateTime timestamp) {
        if (timestamp.getMinute() == 0) {
            return false;
        }

        ZonedDateTime now = ZonedDateTime.now(clock);
        ZonedDateTime zonedTimestamp = timestamp.atZone(ZoneId.of("UTC"));
        return zonedTimestamp.isAfter(now.minus(freshDuration));
    }

    private boolean hasLicense(AppUserDetails userDetails) {
        return userDetails != null
                &&
                userDetails.getAuthorities().contains(new SimpleGrantedAuthority("LICENSE_EUMETSAT"));
    }
}
