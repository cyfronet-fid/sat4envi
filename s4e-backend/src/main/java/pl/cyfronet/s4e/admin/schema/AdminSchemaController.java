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

package pl.cyfronet.s4e.admin.schema;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.schema.SchemaDeletionException;
import pl.cyfronet.s4e.ex.schema.SchemaTypeException;
import pl.cyfronet.s4e.service.SchemaService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/schemas", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "admin-schema", description = "The Admin Schema API")
public class AdminSchemaController {
    private final SchemaService schemaService;

    @Operation(summary = "Create a new Schema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Previous Schema doesn't exist", content = @Content)
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public AdminSchemaResponse create(@RequestBody @Valid AdminCreateSchemaRequest request)
            throws NotFoundException, SchemaTypeException {
        schemaService.create(SchemaService.DTO.builder()
                .name(request.getName())
                .type(request.getType())
                .content(request.getContent())
                .previous(request.getPrevious())
                .build());
        return schemaService.findByName(request.getName(), AdminSchemaResponse.class);
    }

    @Operation(summary = "Get a list of Schemas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public List<AdminSchemaResponse> list() {
        return schemaService.findAllBy(AdminSchemaResponse.class);
    }

    @Operation(summary = "Return Schema content")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(example = "Valid JSON of the Schema"))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Schema doesn't exist", content = @Content)
    })
    @GetMapping("/{name}")
    public AdminSchemaResponse read(@PathVariable String name) throws NotFoundException {
        return schemaService.findByName(name, AdminSchemaResponse.class);
    }

    @Operation(summary = "Update a Schema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Previous Schema doesn't exist", content = @Content)
    })
    @PutMapping(path = "/{name}", consumes = APPLICATION_JSON_VALUE)
    public AdminSchemaResponse update(@PathVariable String name, @RequestBody @Valid AdminUpdateSchemaRequest request)
            throws NotFoundException, SchemaTypeException {
        schemaService.update(SchemaService.DTO.builder()
                .name(name)
                .type(request.getType())
                .content(request.getContent())
                .previous(request.getPrevious())
                .build());
        return schemaService.findByName(name, AdminSchemaResponse.class);
    }

    @Operation(summary = "Delete Schema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "There are existing references to the Schema", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Schema doesn't exist", content = @Content)
    })
    @DeleteMapping("/{name}")
    public void delete(@PathVariable String name) throws SchemaDeletionException, NotFoundException {
        schemaService.deleteByName(name);
    }
}
