package pl.cyfronet.s4e.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.controller.response.SearchResponse;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class ResponseExtender {
    @Autowired
    private TimeHelper timeHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "artifacts", source = "sceneContent")
    public abstract SearchResponse toResponse(MappedScene scene, @Context ZoneId zoneId);

    protected ZonedDateTime getTimestamp(LocalDateTime localDateTime, @Context ZoneId zoneId) {
        return timeHelper.getZonedDateTime(localDateTime, zoneId);
    }

    protected Set<String> getArtifacts(String sceneContent) {
        if (sceneContent == null) {
            return null;
        }

        try {
            JsonNode artifactsNode = objectMapper.readTree(sceneContent).get("artifacts");
            return getKeys(artifactsNode);
        } catch (JsonProcessingException e) {
            log.warn("Cannot parse scene content", e);
            return null;
        }
    }

    private Set<String> getKeys(JsonNode node) {
        HashSet<String> keys = new HashSet<>();
        node.fieldNames().forEachRemaining(keys::add);
        return keys;
    }

    protected JsonNode getMetadata(String metadataContent) {
        if (metadataContent == null) {
            return null;
        }

        try {
            return objectMapper.readTree(metadataContent);
        } catch (JsonProcessingException e) {
            log.warn("Cannot parse scene metadata content", e);
            return null;
        }
    }
}
