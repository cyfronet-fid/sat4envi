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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.controller.request.ZoneParameter;
import pl.cyfronet.s4e.controller.response.SearchResponse;
import pl.cyfronet.s4e.ex.BadRequestException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.QueryException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.SearchService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "api", description = "The Open Search API for scenes")
public class OSearchController {
    private final SearchService searchService;
    private final ResponseExtender responseExtender;

    @Operation(summary = "List scenes for query")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content)
    })
    @GetMapping("/dhus/search")
    public List<SearchResponse> getScenesDHusStyle(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "rows", required = false) String rowsSize,//default =10
            @RequestParam(value = "start", required = false) String rowStart,//default =0
            @RequestParam(value = "format", defaultValue = "") String format,//json/xml?
            @RequestParam(value = "orderby", required = false) String orderby,//beginposition/ingestiondate asc/desc q=?
            @RequestParam(defaultValue = "UTC") ZoneParameter timeZone
    ) throws BadRequestException, SQLException, QueryException, NotFoundException {
        // TODO: format
        try {
            AppUserDetails appUserDetails = AppUserDetailsSupplier.get();
            val params= searchService.parseToParamMap(rowsSize, rowStart, orderby, query);
            searchService.checkProductTypePresent(params);
            searchService.applyLicenseByProductType(params, appUserDetails);
            return searchService.getScenesBy(params).stream()
                    .map(scene -> responseExtender.toResponse(scene, timeZone.getZoneId()))
                    .collect(Collectors.toList());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Cannot parse date: " + e.getParsedString());
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException("Cannot parse query: " + iae.getMessage());
        }
    }

    @Operation(summary = "Count scenes for query")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved count"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content)
    })
    @GetMapping("/dhus/search/count")
    public Long getCountDHusStyle(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(defaultValue = "UTC") ZoneParameter timeZone
    ) throws BadRequestException, SQLException, QueryException, NotFoundException {
        // TODO: format
        try {
            AppUserDetails appUserDetails = AppUserDetailsSupplier.get();
            val params = searchService.countParseToParamMap(query);
            searchService.checkProductTypePresent(params);
            searchService.applyLicenseByProductType(params, appUserDetails);
            return searchService.getCountBy(params);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Cannot parse date: " + e.getParsedString());
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException("Cannot parse query: " + iae.getMessage());
        }
    }
}
