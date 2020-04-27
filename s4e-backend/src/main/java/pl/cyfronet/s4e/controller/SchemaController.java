package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.CreateSchemaRequest;
import pl.cyfronet.s4e.controller.response.SchemaResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.SchemaCreationException;
import pl.cyfronet.s4e.ex.SchemaDeletionException;
import pl.cyfronet.s4e.service.SchemaService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "schema", description = "The Schema API")
public class SchemaController {
    private final SchemaService schemaService;

    @Operation(summary = "Create a new Schema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Previous Schema doesn't exist", content = @Content)
    })
    @PostMapping(value = "/schema", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() && isAdmin()")
    public SchemaResponse create(@RequestBody @Valid CreateSchemaRequest request)
            throws NotFoundException, SchemaCreationException {
        schemaService.create(SchemaService.Create.builder()
                .name(request.getName())
                .type(request.getType())
                .content(request.getContent())
                .previous(request.getPrevious())
                .build());
        return schemaService.findByName(request.getName(), SchemaResponse.class);
    }

    @Operation(summary = "Get a list of Schemas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/schema")
    public List<SchemaResponse> findAll() {
        return schemaService.findAllBy(SchemaResponse.class);
    }

    @Operation(summary = "Return Schema content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(example = "Valid JSON of the Schema"))),
            @ApiResponse(responseCode = "404", description = "Schema doesn't exist", content = @Content)
    })
    @GetMapping("/schema/{name}")
    public String get(@PathVariable String name) throws NotFoundException {
        return schemaService.getContentByName(name);
    }

    @Operation(summary = "Delete Schema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "There are existing references to the Schema", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Schema doesn't exist", content = @Content)
    })
    @DeleteMapping("/schema/{name}")
    @PreAuthorize("isAuthenticated() && isAdmin()")
    public void delete(@PathVariable String name) throws SchemaDeletionException, NotFoundException {
        schemaService.deleteByName(name);
    }
}
