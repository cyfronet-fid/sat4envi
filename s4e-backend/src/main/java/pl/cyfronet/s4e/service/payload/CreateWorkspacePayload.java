package pl.cyfronet.s4e.service.payload;

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
public class CreateWorkspacePayload {
    @Value
    @Builder
    private static class Workspace {
        String name;
    }

    Workspace workspace;

    public CreateWorkspacePayload(String workspace) {
        this.workspace = Workspace.builder()
                .name(workspace)
                .build();
    }
}
