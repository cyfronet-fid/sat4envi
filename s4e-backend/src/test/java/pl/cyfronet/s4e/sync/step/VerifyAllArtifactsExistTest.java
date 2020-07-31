package pl.cyfronet.s4e.sync.step;

import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;
import static pl.cyfronet.s4e.sync.Error.*;

class VerifyAllArtifactsExistTest extends BaseStepTest<BaseContext> {
    @Mock
    private SceneStorage sceneStorage;

    @Mock
    private BiConsumer<BaseContext, Map<String, String>> update;

    @Mock
    private JsonObject sceneJson;

    private VerifyAllArtifactsExist step;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        step = VerifyAllArtifactsExist.builder()
                .sceneJson(c -> sceneJson)
                .sceneStorage(() -> sceneStorage)
                .update(update)
                .build();
    }

    @Test
    public void shouldWork() throws S3ClientException {
        JsonObject artifactsJsonObject = mock(JsonObject.class);
        Map<String, JsonValue> artifacts = Map.of(
                "key_1", new TestJsonString("/path_1"),
                "key_2", new TestJsonString("/path_2")
        );
        when(artifactsJsonObject.entrySet()).thenReturn(artifacts.entrySet());
        when(sceneJson.getJsonObject(SCENE_SCHEMA_ARTIFACTS_KEY)).thenReturn(artifactsJsonObject);

        when(sceneStorage.exists("path_1")).thenReturn(true);
        when(sceneStorage.exists("path_2")).thenReturn(true);

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(eq(context), eq(Map.of("key_1", "path_1", "key_2", "path_2")));
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldVerifyAllPathsAbsolute() throws S3ClientException {
        JsonObject artifactsJsonObject = mock(JsonObject.class);
        Map<String, JsonValue> artifacts = Map.of(
                "key_1", new TestJsonString("path_1"),
                "key_2", new TestJsonString("/path_2")
        );
        when(artifactsJsonObject.entrySet()).thenReturn(artifacts.entrySet());
        when(sceneJson.getJsonObject(SCENE_SCHEMA_ARTIFACTS_KEY)).thenReturn(artifactsJsonObject);

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_ARTIFACT_PATH_INCORRECT)));
        assertThat(error.getParameters(), hasEntry("artifact_key_1", "path_1"));
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldVerifyAllArtifactsExist() throws S3ClientException {
        JsonObject artifactsJsonObject = mock(JsonObject.class);
        Map<String, JsonValue> artifacts = Map.of(
                "key_1", new TestJsonString("/path_1"),
                "key_2", new TestJsonString("/path_2"),
                "key_3", new TestJsonString("/path_3")
        );
        when(artifactsJsonObject.entrySet()).thenReturn(artifacts.entrySet());
        when(sceneJson.getJsonObject(SCENE_SCHEMA_ARTIFACTS_KEY)).thenReturn(artifactsJsonObject);

        when(sceneStorage.exists("path_1")).thenReturn(false);
        when(sceneStorage.exists("path_2")).thenReturn(true);
        when(sceneStorage.exists("path_3")).thenReturn(false);

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_ARTIFACTS_NOT_FOUND)));
        assertThat(error.getParameters(), allOf(
                hasEntry("artifact_key_1", "path_1"),
                hasEntry("artifact_key_3", "path_3")
        ));
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldHandleS3ClientException() throws S3ClientException {
        JsonObject artifactsJsonObject = mock(JsonObject.class);
        Map<String, JsonValue> artifacts = Map.of(
                "key_1", new TestJsonString("/path_1")
        );
        when(artifactsJsonObject.entrySet()).thenReturn(artifacts.entrySet());
        when(sceneJson.getJsonObject(SCENE_SCHEMA_ARTIFACTS_KEY)).thenReturn(artifactsJsonObject);

        when(sceneStorage.exists("path_1")).thenThrow(S3ClientException.class);

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_S3_CLIENT_EXCEPTION)));
        assertThat(error.getParameters(), allOf(
                hasEntry("artifact_key_1", "path_1")
        ));
        verifyNoMoreInteractions(update);
    }

    @Value
    private static class TestJsonString implements JsonString {
        String string;
        CharSequence chars = null;
        ValueType valueType = ValueType.STRING;
    }
}
