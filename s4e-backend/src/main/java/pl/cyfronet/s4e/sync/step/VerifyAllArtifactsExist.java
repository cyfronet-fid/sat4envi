package pl.cyfronet.s4e.sync.step;

import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.sync.Error.*;

@Builder
public class VerifyAllArtifactsExist<T extends BaseContext> implements Step<T, Error> {
    public static final String SCENE_ARTIFACTS_PROPERTY = "artifacts";

    private final Supplier<SceneStorage> sceneStorage;

    private final Function<T, JsonObject> sceneJson;
    private final BiConsumer<T, Map<String, String>> update;

    @Override
    public Error apply(T context) {
        val error = context.getError();
        SceneStorage sceneStorage = this.sceneStorage.get();

        JsonObject sceneJson = this.sceneJson.apply(context);

        val artifacts = new HashMap<String, String>();

        JsonObject artifactsObject = sceneJson.getJsonObject(SCENE_ARTIFACTS_PROPERTY);
        Map<String, String> notFound = new LinkedHashMap<>();
        for (val entry : artifactsObject.entrySet()) {
            String name = entry.getKey();
            String path = ((JsonString) entry.getValue()).getString();
            if (!isPathCorrect(path)) {
                return error.code(ERR_ARTIFACT_PATH_INCORRECT)
                        .parameter("artifact_" + name, path).build();
            }
            artifacts.put(name, path);
            try {
                if (!sceneStorage.exists(path)) {
                    notFound.put(name, path);
                }
            } catch (S3ClientException e) {
                return error.code(ERR_S3_CLIENT_EXCEPTION)
                        .parameter("artifact_" + name, path).build();
            }
        }
        if (!notFound.isEmpty()) {
            notFound.forEach((k,v) -> error.parameter("artifact_" + k, v));
            return error.code(ERR_ARTIFACTS_NOT_FOUND).build();
        }

        update.accept(context, artifacts);

        return null;
    }

    private boolean isPathCorrect(String path) {
        return path.startsWith("/");
    }
}