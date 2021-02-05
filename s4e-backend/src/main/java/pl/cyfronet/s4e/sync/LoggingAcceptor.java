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

package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.cyfronet.s4e.sync.context.Context;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class LoggingAcceptor implements SceneAcceptor {
    private final SceneAcceptor delegate;

    @Override
    public Error accept(Context context) {
        Error error = delegate.accept(context);
        if (error != null) {
            handleError(error);
        }
        return error;
    }

    private void handleError(Error error) {
        StringBuilder sb = createBuilderAndAppendSceneKey(error.getSceneKey());
        appendCode(sb, error.getCode());
        appendParameters(sb, error.getParameters());
        log.warn(sb.toString(), error.getCause());
    }

    private StringBuilder createBuilderAndAppendSceneKey(String key) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error processing key: '").append(key).append("'.");
        return sb;
    }

    private void appendCode(StringBuilder sb, String code) {
        if (code != null) {
            sb.append(" Error code: ").append(code).append(".");
        }
    }

    private void appendParameters(StringBuilder sb, Map<String, Object> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            List<String> mappedParameters = parameters.entrySet().stream()
                    .map(e -> e.getKey() + "=>" + e.getValue())
                    .collect(Collectors.toList());
            sb.append(" Parameters: ").append(String.join(", ", mappedParameters)).append(".");
        }
    }
}
