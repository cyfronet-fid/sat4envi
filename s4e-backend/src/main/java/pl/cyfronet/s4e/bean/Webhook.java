package pl.cyfronet.s4e.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder
public class Webhook {
    @JsonProperty("EventName")
    String eventName;
    @JsonProperty("Key")
    String key;
}
