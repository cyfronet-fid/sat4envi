package pl.cyfronet.s4e.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;

@Service
@RequiredArgsConstructor
@Validated
public class SceneArtifactsHelper {
    private final SceneRepository sceneRepository;

    interface SceneProjection {
        Long getId();
        JsonNode getSceneContent();
    }

    public String getArtifact(@NotNull Long sceneId, @NotBlank String artifactName) throws NotFoundException {
        val sceneProjection = sceneRepository.findById(sceneId, SceneProjection.class)
                .orElseThrow(constructNFE(sceneId));
        try {
            return sceneProjection.getSceneContent().get(SCENE_SCHEMA_ARTIFACTS_KEY).get(artifactName).asText().substring(1);
        } catch (NullPointerException e) {
            throw new NotFoundException("Artifact with name '" + artifactName + "' for Scene with id '" + sceneId + "' not found");
        }
    }

    private Supplier<NotFoundException> constructNFE(Long id) {
        return () -> new NotFoundException("Scene with id '" + id + "' not found");
    }
}
