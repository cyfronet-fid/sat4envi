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

package pl.cyfronet.s4e.geoserver.op.request;

import lombok.Builder;
import lombok.Value;


/**
 * <pre>
 *     {
 *         "workspace": {
 *             "name": "{name}"
 *         }
 *     }
 * </pre>
 */
@Value
public class CreateWorkspaceRequest {
    @Value
    @Builder
    private static class Workspace {
        String name;
    }

    Workspace workspace;

    public CreateWorkspaceRequest(String workspace) {
        this.workspace = Workspace.builder()
                .name(workspace)
                .build();
    }
}
