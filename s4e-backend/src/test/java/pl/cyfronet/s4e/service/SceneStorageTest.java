package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.properties.S3Properties;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SceneStorageTest {
    @Mock
    private S3Properties s3Properties;
    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private SceneRepository sceneRepository;

    @InjectMocks
    private SceneStorage sceneStorage;

    @Test
    public void shouldThrowNotFoundExceptionIfSceneNotFound() {
        when(sceneRepository.findById(42L, SceneStorage.SceneProjection.class)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sceneStorage.generatePresignedGetLink(42L, Duration.ZERO));
    }

    @Test
    public void shouldThrowAnIllegalStateExceptionIfThePresignedRequestNotBrowserExecutable() {
        SceneStorage.SceneProjection scene = mock(SceneStorage.SceneProjection.class);
        when(scene.getS3Path()).thenReturn("some/path");
        when(sceneRepository.findById(42L, SceneStorage.SceneProjection.class))
                .thenReturn(Optional.of(scene));

        PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
        when(pgor.isBrowserExecutable()).thenReturn(false);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

        assertThrows(IllegalStateException.class, () -> sceneStorage.generatePresignedGetLink(42L, Duration.ZERO));
    }

    @Test
    public void shouldReturnURL() throws NotFoundException {
        SceneStorage.SceneProjection scene = mock(SceneStorage.SceneProjection.class);
        when(scene.getS3Path()).thenReturn("some/path");
        when(sceneRepository.findById(42L, SceneStorage.SceneProjection.class))
                .thenReturn(Optional.of(scene));

        PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
        when(pgor.isBrowserExecutable()).thenReturn(true);
        URL url = mock(URL.class);
        when(pgor.url()).thenReturn(url);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

        assertEquals(url, sceneStorage.generatePresignedGetLink(42L, Duration.ZERO));
    }
}
