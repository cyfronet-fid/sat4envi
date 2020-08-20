package pl.cyfronet.s4e.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
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

    public String getArtifact(Long id, String type) throws NotFoundException {
        if (type == null) {
            SceneExtendedProjection sceneExtendedProjection = sceneExtendedRepository.findById(id, SceneExtendedProjection.class)
                    .orElseThrow(constructNFE(id));
            return sceneExtendedProjection.getS3Path();
        }

        SceneProjection sceneProjection = sceneRepository.findById(id, SceneProjection.class)
                .orElseThrow(constructNFE(id));
        try {
            return sceneProjection.getSceneContent().get(SCENE_SCHEMA_ARTIFACTS_KEY).get(type).asText().substring(1);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private Supplier<NotFoundException> constructNFE(Long id) {
        return () -> new NotFoundException("Scene with id '" + id + "' not found");
    }
}
