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

package pl.cyfronet.s4e.sync.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.Error.ERR_FILE_NOT_FOUND;
import static pl.cyfronet.s4e.sync.Error.ERR_S3_CLIENT_EXCEPTION;

class LoadKeyContentTest extends BaseStepTest<BaseContext> {
    @Mock
    private SceneStorage sceneStorage;

    @Mock
    private BiConsumer<BaseContext, String> update;

    private LoadKeyContent step;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        step = LoadKeyContent.builder()
                .sceneStorage(() -> sceneStorage)
                .key(c -> sceneKey)
                .update(update)
                .build();
    }

    @Test
    public void shouldWork() throws NotFoundException, S3ClientException {
        String content = "{\"content\":42}";
        when(sceneStorage.get(sceneKey)).thenReturn(content);

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(sceneStorage).get(sceneKey);
        verify(update).accept(context, content);
        verifyNoMoreInteractions(sceneStorage, update);
    }

    @Test
    public void shouldHandleNFE() throws NotFoundException, S3ClientException {
        when(sceneStorage.get(sceneKey)).thenThrow(NotFoundException.class);

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_FILE_NOT_FOUND)));
        assertThat(error.getCause(), is(instanceOf(NotFoundException.class)));
        verify(sceneStorage).get(sceneKey);
        verifyNoMoreInteractions(sceneStorage, update);
    }

    @Test
    public void shouldHandleS3ClientException() throws NotFoundException, S3ClientException {
        when(sceneStorage.get(sceneKey)).thenThrow(S3ClientException.class);

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_S3_CLIENT_EXCEPTION)));
        assertThat(error.getCause(), is(instanceOf(S3ClientException.class)));
        verify(sceneStorage).get(sceneKey);
        verifyNoMoreInteractions(sceneStorage, update);
    }
}
