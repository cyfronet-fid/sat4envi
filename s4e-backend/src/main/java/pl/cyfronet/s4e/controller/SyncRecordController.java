/*
 * Copyright 2021 ACC Cyfronet AGH
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

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringExpression;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.bean.QSyncRecord;
import pl.cyfronet.s4e.bean.SyncRecord;
import pl.cyfronet.s4e.data.repository.SyncRecordRepository;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1 + "/sync-records", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "sync-records", description = "The Sync Records API")
public class SyncRecordController implements QuerydslBinderCustomizer<QSyncRecord> {
    private final SyncRecordRepository syncRecordRepository;

    @Operation(summary = "Search for SyncRecords")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @GetMapping
    public Page<SyncRecord> search(
            @QuerydslPredicate(root = SyncRecord.class, bindings = SyncRecordController.class) Predicate predicate,
            @ParameterObject Pageable pageable
    ) {
        return syncRecordRepository.findAll(predicate, pageable);
    }

    @Operation(summary = "Delete SyncRecords")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Incorrect request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @QuerydslPredicate(root = SyncRecord.class, bindings = SyncRecordController.class) Predicate predicate
    ) {
        Page<SyncRecord> page = syncRecordRepository.findAll(predicate, PageRequest.of(0, 1000));
        while (page.hasContent()) {
            syncRecordRepository.deleteAll(page.getContent());
            page = syncRecordRepository.findAll(predicate, PageRequest.of(0, 1000));
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    public void customize(QuerydslBindings bindings, QSyncRecord root) {
        bindings.bind(
                root.initiatedByMethod,
                root.sceneKey,
                root.eventName,
                root.resultCode
        ).first(StringExpression::startsWith);
        bindings.bind(root.receivedAtFrom).first((path, value) -> root.receivedAt.goe(value));
        bindings.bind(root.receivedAtTo).first((path, value) -> root.receivedAt.lt(value));
        bindings.bind(root.sensingTimeFrom).first((path, value) -> root.sensingTime.goe(value));
        bindings.bind(root.sensingTimeTo).first((path, value) -> root.sensingTime.lt(value));
        bindings.excluding(
                root.receivedAt,
                root.sensingTime,
                root.exceptionMessage,
                root.parameters
        );
    }
}
