package pl.cyfronet.s4e.sync;

import org.springframework.data.web.JsonPath;

public interface Notification {
    @JsonPath("$.Records[0].eventName")
    String getEventName();

    @JsonPath("$.Records[0].s3.object.key")
    String getObjectKey();
}
