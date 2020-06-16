package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SceneFileStorageService {

    private final SceneRepository sceneRepository;
    private final ObjectMapper objectMapper;

    interface SceneProjection {
        JsonNode getSceneContent();
    }

    public Map<String, String> getSceneArtifacts(Long id) throws NotFoundException {
        SceneProjection sceneProjection = sceneRepository.findById(id, SceneProjection.class)
                .orElseThrow(() -> new NotFoundException("Scene with id '" + id + "' not found"));
        return  objectMapper.convertValue(
                sceneProjection.getSceneContent().get("artifacts"),
                new TypeReference<>() {});
    }
}
