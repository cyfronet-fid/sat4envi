package pl.cyfronet.s4e.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.SceneExtendedRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.function.Supplier;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;

@Service
@RequiredArgsConstructor
public class SceneArtifactsHelper {
    private final SceneRepository sceneRepository;
    private final SceneExtendedRepository sceneExtendedRepository;

    interface SceneProjection {
        Long getId();
        JsonNode getSceneContent();
    }

    interface SceneExtendedProjection {
        Long getId();
        String getS3Path();
    }

    public String getArtifact(Long sceneId, String artifactName) throws NotFoundException {
        if (artifactName == null) {
            val sceneExtendedProjection = sceneExtendedRepository.findById(sceneId, SceneExtendedProjection.class)
                    .orElseThrow(constructNFE(sceneId));
            return sceneExtendedProjection.getS3Path();
        }

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
