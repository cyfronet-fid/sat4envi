/*
 * Copyright 2021 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;

class ZipArtifactTest {
    @Test
    public void shouldReturnArtifactName() {
        assertThat(
                ZipArtifact.getName(Map.of("foo", "etc", "baz", "path.zip", "bar", "another.zip")),
                isPresentAndIs("bar")
        );
    }

    @Test
    public void shouldReturnEmpty() {
        assertThat(
                ZipArtifact.getName(Map.of("foo", "etc")),
                isEmpty()
        );
    }

    @Test
    public void shouldHandleEmptyMap() {
        assertThat(
                ZipArtifact.getName(Map.of()),
                isEmpty()
        );
    }

    @Test
    public void shouldHandleNull() {
        assertThat(
                ZipArtifact.getName((Map<String, String>) null),
                isEmpty()
        );
    }
}
