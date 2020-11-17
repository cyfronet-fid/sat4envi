package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateReportTemplateRequest {
    String caption;

    String notes;

    List<Long> overlayIds;

    Long productId;
}
