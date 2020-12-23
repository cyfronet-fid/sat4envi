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

package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.UserAuthorityRequest;
import pl.cyfronet.s4e.controller.response.UserAuthorityResponse;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.UserAuthorityService;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@Slf4j
@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "user-authority", description = "The UserAuthority API")
public class UserAuthorityController {
    private final UserAuthorityService userAuthorityService;

    @Operation(summary = "Get users with authority")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns user with authority"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/users/authority/{authority}")
    public List<UserAuthorityResponse> getUsersWithAuthority(@PathVariable String authority) {
        return userAuthorityService.findAllUsersByAuthority(authority, UserAuthorityResponse.class);
    }

    @Operation(summary = "Add authority to user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Added authority to user"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User doesn't exist")
    })
    @Transactional(rollbackFor = NotFoundException.class)
    @PutMapping("/users/authority/{authority}")
    @Consumes(APPLICATION_JSON_VALUE)
    public void addAuthority(
            @PathVariable String authority,
            @RequestBody @Valid UserAuthorityRequest request
    ) throws NotFoundException {
        userAuthorityService.addAuthority(request.getEmail(), authority)
                .orElseThrow(NotFoundException::new);
    }

    @Operation(summary = "Remove authority from user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Removed authority from user"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User doesn't exist")
    })
    @Transactional(rollbackFor = NotFoundException.class)
    @DeleteMapping("/users/authority/{authority}")
    @Consumes(APPLICATION_JSON_VALUE)
    public void removeAuthority(
            @PathVariable String authority,
            @RequestBody @Valid UserAuthorityRequest request
    ) throws NotFoundException {
        userAuthorityService.removeAuthority(request.getEmail(), authority)
                .orElseThrow(NotFoundException::new);
    }
}
