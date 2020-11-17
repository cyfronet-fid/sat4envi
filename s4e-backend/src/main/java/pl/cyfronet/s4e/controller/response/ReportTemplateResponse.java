package pl.cyfronet.s4e.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;

import java.time.ZonedDateTime;
import java.util.List;

public interface ReportTemplateResponse {
    @Value("#{target.id}")
    @Schema(format = "uuid")
    String getUuid();

    String getCaption();

    String getNotes();

    List<Long> getOverlayIds();

    Long getProductId();

    @Value("#{@timeHelper.getZonedDateTimeWithBaseZone(target.createdAt)}")
    ZonedDateTime getCreatedAt();
}
