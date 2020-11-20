package pl.cyfronet.s4e.admin.license;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUpdateLicenseGrantRequest {
    private boolean owner;
}
