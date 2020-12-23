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

package pl.cyfronet.s4e.admin.scene;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.SceneService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "admin-scene", description = "The Admin Scene API")
public class AdminSceneController {
    private static final String DEFAULT_SORT = "timestamp";

    private final SceneService sceneService;

    private final AdminSceneMapper adminSceneMapper;

    @Operation(summary = "Return Scene")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Scene doesn't exist", content = @Content)
    })
    @GetMapping("/scenes/{id}")
    public AdminSceneResponse read(@PathVariable Long id) throws NotFoundException {
        return sceneService.findById(id, AdminSceneProjection.class)
                .map(adminSceneMapper::projectionToResponse)
                .orElseThrow(() -> new NotFoundException("Scene with id '" + id + "' not found"));
    }

    @Operation(summary = "Return Scenes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PageableAsQueryParam
    @GetMapping("/scenes")
    public Page<AdminSceneResponse> list(
            @Parameter(hidden = true) @SortDefault(sort = DEFAULT_SORT, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return sceneService.list(pageable, AdminSceneProjection.class)
                .map(adminSceneMapper::projectionToResponse);
    }

    @Operation(summary = "Return Scenes by Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product doesn't exist", content = @Content)
    })
    @PageableAsQueryParam
    @GetMapping("/products/{productId}/scenes")
    public Page<AdminSceneResponse> listByProduct(
            @PathVariable Long productId,
            @Parameter(hidden = true) @SortDefault(sort = DEFAULT_SORT, direction = Sort.Direction.DESC) Pageable pageable
    ) throws NotFoundException {
        return sceneService.listByProduct(productId, pageable, AdminSceneProjection.class)
                .map(adminSceneMapper::projectionToResponse);
    }

    @Operation(summary = "Delete Scene")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Scene doesn't exist", content = @Content)
    })
    @DeleteMapping("/scenes/{id}")
    public void delete(@PathVariable Long id) throws NotFoundException {
        sceneService.delete(id);
    }

    @Operation(summary = "Delete all Scenes of a Product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product doesn't exist", content = @Content)
    })
    @DeleteMapping("/products/{productId}/scenes")
    public void deleteProductScenes(@PathVariable Long productId) throws NotFoundException {
        sceneService.deleteProductScenes(productId);
    }
}
