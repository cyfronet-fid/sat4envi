package pl.cyfronet.s4e.admin.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.controller.validation.JsonString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
class AdminCreateSchemaRequest {
    @NotEmpty
    @Schema(description = "The Schema name", required = true, example = "MSG.scene.v2.json")
    private String name;

    @NotNull
    private pl.cyfronet.s4e.bean.Schema.Type type;

    @NotEmpty
    @JsonString
    @Schema(description = "The Schema content. Must be valid JSON.")
    private String content;

    @Schema(description = "This Schema predecessor", required = false, example = "MSG.scene.v1.json")
    private String previous;
}
