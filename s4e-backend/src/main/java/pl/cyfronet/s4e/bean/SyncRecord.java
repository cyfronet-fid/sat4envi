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

package pl.cyfronet.s4e.bean;

import com.querydsl.core.annotations.QueryEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)

@QueryEntity

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SyncRecord {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String initiatedByMethod;

    private String sceneKey;

    private String eventName;

    private LocalDateTime receivedAt;

    private LocalDateTime sensingTime;

    private String productName;

    private String resultCode;

    private String exceptionMessage;

    @Type(type = "jsonb")
    @ToString.Exclude
    private Map<String, Object> parameters;

    // The following are necessary for QueryDSL to use date-time ranges.

    @Transient @Getter(AccessLevel.PRIVATE)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime receivedAtFrom;

    @Transient @Getter(AccessLevel.PRIVATE)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime receivedAtTo;

    @Transient @Getter(AccessLevel.PRIVATE)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime sensingTimeFrom;

    @Transient @Getter(AccessLevel.PRIVATE)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime sensingTimeTo;
}
