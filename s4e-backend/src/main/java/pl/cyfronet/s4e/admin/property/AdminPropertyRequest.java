package pl.cyfronet.s4e.admin.property;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class AdminPropertyRequest {
    private String value;
}
