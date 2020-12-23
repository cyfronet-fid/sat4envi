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

package pl.cyfronet.s4e.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.SceneFileStorageService;
import pl.cyfronet.s4e.service.SceneStorage;

import java.net.URISyntaxException;
import java.net.URL;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "api", description = "The OData API for scenes")
public class ODataController {
    private final SceneStorage sceneStorage;
    private final SceneFileStorageService sceneFileStorageService;

    @Operation(summary = "Redirect to a presigned download url for an archive")
    @ApiResponses({
            @ApiResponse(responseCode = "303", description = "Redirect to the presigned download url", content = @Content,
                    headers = @Header(name = "Location", description = "The presigned download url")),
            @ApiResponse(responseCode = "404", description = "Scene not found", content = @Content)
    })
    @GetMapping(value = "/dhus/odata/v1/Products('{sceneId}')/$value")
    public ResponseEntity<Void> generateDownloadLinkArchive(@PathVariable Long sceneId)
            throws NotFoundException, URISyntaxException {
        URL downloadLink = sceneStorage.generatePresignedGetLinkWithFileType(
                sceneId,
                sceneFileStorageService.getSceneArtifacts(sceneId)
                        .entrySet().stream()
                        .filter(map -> map.getValue().contains(".zip"))
                        .findFirst()
                        .get().getKey(),
                sceneStorage.getPresignedGetTimeout()
        );
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(downloadLink.toURI()).build();
    }

    @Operation(summary = "Redirect to a presigned download url")
    @ApiResponses({
            @ApiResponse(responseCode = "303", description = "Redirect to the presigned download url", content = @Content,
                    headers = @Header(name = "Location", description = "The presigned download url")),
            @ApiResponse(responseCode = "404", description = "Scene or artifact not found", content = @Content)
    })
    @GetMapping(value = "/dhus/odata/v1/Products('{sceneId}')/Nodes('{ignored}')/Nodes('{artifactName}')/$value")
    public ResponseEntity<Void> generateDownloadLinkType(@PathVariable Long sceneId,
                                                         @PathVariable String artifactName)
            throws NotFoundException, URISyntaxException {
        URL downloadLink = sceneStorage.generatePresignedGetLinkWithFileType(
                sceneId,
                artifactName,
                sceneStorage.getPresignedGetTimeout()
        );
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(downloadLink.toURI()).build();
    }
}
