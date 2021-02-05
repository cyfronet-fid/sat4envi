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

package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pl.cyfronet.s4e.bean.SyncRecord;
import pl.cyfronet.s4e.data.repository.SyncRecordRepository;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;
import pl.cyfronet.s4e.service.SceneService;
import pl.cyfronet.s4e.sync.context.Context;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
public class ContextRecorder {
    private interface SceneProjection extends ProjectionWithId {
        LocalDateTime getTimestamp();
    }

    private final SyncRecordRepository syncRecordRepository;
    private final SceneService sceneService;

    public void record(Context context, Error error) {
        try {
            val syncRecordBuilder = SyncRecord.builder()
                    .initiatedByMethod(context.getInitiatedByMethod())
                    .sceneKey(context.getScene().getKey())
                    .eventName(context.getEventName())
                    .receivedAt(context.getReceivedAt());

            if (context.getSceneId() != null) {
                sceneService.findById(context.getSceneId(), SceneProjection.class)
                        .ifPresent(scene -> syncRecordBuilder.sensingTime(scene.getTimestamp()));
            }

            if (context.getProduct() != null) {
                syncRecordBuilder.productName(context.getProduct().getName());
            }
            if (error != null) {
                syncRecordBuilder.resultCode("err:" + error.getCode());
                syncRecordBuilder.parameters(error.getParameters());
                if (error.getCause() != null) {
                    syncRecordBuilder.exceptionMessage(error.getCause().getMessage());
                }
            } else {
                syncRecordBuilder.resultCode("ok");
            }
            syncRecordRepository.save(syncRecordBuilder.build());
        } catch (Exception e) {
            log.warn("Unexpected exception when recording sync status", e);
        }
    }
}
