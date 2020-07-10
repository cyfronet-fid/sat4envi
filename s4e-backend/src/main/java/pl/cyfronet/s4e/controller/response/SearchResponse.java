package pl.cyfronet.s4e.controller.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
public class SearchResponse {
    private Long id;
    private Long productId;
    private String footprint;
    private Set<String> artifacts;
    private JsonNode metadataContent;
    private ZonedDateTime timestamp;
}
