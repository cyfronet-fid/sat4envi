package pl.cyfronet.s4e.service.request;

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
