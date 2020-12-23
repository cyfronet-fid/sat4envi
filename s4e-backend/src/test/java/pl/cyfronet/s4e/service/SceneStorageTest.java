/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.properties.S3Properties;
import pl.cyfronet.s4e.util.SceneArtifactsHelper;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

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
    private SceneArtifactsHelper sceneArtifactsHelper;

    @InjectMocks
    private SceneStorage sceneStorage;

    @Test
    public void shouldThrowNotFoundExceptionIfSceneNotFound() throws NotFoundException {
        when(sceneArtifactsHelper.getArtifact(42L, null)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> sceneStorage.generatePresignedGetLink(42L, Duration.ZERO));
    }

    @Test
    public void shouldThrowAnIllegalStateExceptionIfThePresignedRequestNotBrowserExecutable() throws NotFoundException {
        when(sceneArtifactsHelper.getArtifact(42L, null)).thenReturn("some/path");

        PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
        when(pgor.isBrowserExecutable()).thenReturn(false);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

        assertThrows(IllegalStateException.class, () -> sceneStorage.generatePresignedGetLink(42L, Duration.ZERO));
    }

    @Test
    public void shouldReturnURL() throws NotFoundException {
        when(sceneArtifactsHelper.getArtifact(42L, null)).thenReturn("some/path");

        PresignedGetObjectRequest pgor = mock(PresignedGetObjectRequest.class);
        when(pgor.isBrowserExecutable()).thenReturn(true);
        URL url = mock(URL.class);
        when(pgor.url()).thenReturn(url);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(pgor);

        assertEquals(url, sceneStorage.generatePresignedGetLink(42L, Duration.ZERO));
    }
}
