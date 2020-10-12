package pl.cyfronet.s4e.license.types;

import pl.cyfronet.s4e.security.AppUserDetails;

import java.time.LocalDateTime;

public interface TimestampGranularLicense extends ProductGranularLicense {
    /**
     * Decide whether to allow reading a scene with timestamp by user.
     * <p>
     * No further information fetching should occur in implementations.
     * <p>
     * This method should only be called after verifying the {@link ProductGranularLicense} for an access request.
     *
     *
     * @param timestamp
     * @param userDetails
     * @return
     */
    boolean canRead(LocalDateTime timestamp, AppUserDetails userDetails);
}
