package pl.cyfronet.s4e.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder
public class WebhookRequest {
    @JsonProperty("EventName")
    String eventName;
    @JsonProperty("Key")
    String key;
}
