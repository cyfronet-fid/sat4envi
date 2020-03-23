package pl.cyfronet.s4e.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

public interface SchemaResponse {
    interface Previous {
        @Schema(description = "The previous Schema name", example = "MSG.scene.v1.json")
        String getName();
    }

    Long getId();

    @Schema(description = "The Schema name", example = "MSG.scene.v2.json")
    String getName();

    pl.cyfronet.s4e.bean.Schema.Type getType();

    @Schema(description = "The predecessor of this Schema", nullable = true)
    Previous getPrevious();
}
