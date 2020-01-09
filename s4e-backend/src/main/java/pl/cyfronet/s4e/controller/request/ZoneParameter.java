package pl.cyfronet.s4e.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;

/**
 * Make sure to use <code>@RequestParam(defaultValue = "UTC")</code> when using it as a request param to match schema.
 */
@RequiredArgsConstructor
@Getter
@Schema(type = "string", format = "timezone", example = "Europe/Warsaw", defaultValue = "UTC")
public class ZoneParameter {
    private final ZoneId zoneId;
}
