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

package pl.cyfronet.s4e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResourceHelper {
    private final ResourceLoader resourceLoader;

    public byte[] getAsBytes(String path) {
        try (InputStream inputStream = getAsInputStream(path)) {
            return inputStream.readAllBytes();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            log.warn("Couldn't read file at path '" + path + "'", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * The stream has to be closed after use.
     * @param path
     * @return
     * @throws IOException
     */
    public InputStream getAsInputStream(String path) throws IOException {
        return resourceLoader.getResource(path).getInputStream();
    }

    public String getAsStringInBase64(String path) {
        return Base64.getEncoder().encodeToString(getAsBytes(path));
    }
}
