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
