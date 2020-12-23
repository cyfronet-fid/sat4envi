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

package pl.cyfronet.s4e.admin.property;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.bean.Property;
import pl.cyfronet.s4e.data.repository.PropertyRepository;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;

@RestController
@RequestMapping(path = ADMIN_PREFIX + "/properties", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "admin-property", description = "The Admin Property API")
public class AdminPropertyController {
    private final PropertyRepository propertyRepository;

    @Operation(summary = "Create or update a Property")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PutMapping(path = "/{name}", consumes = APPLICATION_JSON_VALUE)
    @Transactional
    public AdminPropertyResponse put(
            @PathVariable String name,
            @RequestBody @Valid AdminPropertyRequest request
    ) {
        Optional<Property> optionalProperty = propertyRepository.findById(name);
        if (optionalProperty.isPresent()) {
            optionalProperty.get().setValue(request.getValue());
        } else {
            propertyRepository.save(Property.builder()
                    .name(name)
                    .value(request.getValue())
                    .build());
        }
        return new AdminPropertyResponse() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getValue() {
                return request.getValue();
            }
        };
    }

    @Operation(summary = "Get a list of Properties")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public List<AdminPropertyResponse> list() {
        return propertyRepository.findAllBy(AdminPropertyResponse.class);
    }

    @Operation(summary = "Delete a Property")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Schema doesn't exist", content = @Content)
    })
    @DeleteMapping("/{name}")
    public void delete(@PathVariable String name) {
        propertyRepository.deleteByName(name);
    }
}
