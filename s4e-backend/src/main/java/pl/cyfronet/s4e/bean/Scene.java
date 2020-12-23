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

package pl.cyfronet.s4e.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.*;
import org.locationtech.jts.geom.Geometry;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * There is ON CASCADE DELETE on Product
 */
@Entity
@DynamicUpdate
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Scene extends CreationAndModificationAudited {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_SCENE_KEY = "scene_key";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_FOOTPRINT = "footprint";
    public static final String COLUMN_METADATA = "metadata_content";
    public static final String COLUMN_CONTENT = "scene_content";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    /// E.g. "path/to/granule.tiff", excluding endpoint and bucket information
    @NotEmpty
    private String sceneKey;

    @Generated(GenerationTime.ALWAYS)
    private LocalDateTime timestamp;

    @NotNull
    private Geometry footprint;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @ToString.Exclude
    private Legend legend;

    @Type(type = "jsonb")
    @NotNull
    @ToString.Exclude
    private JsonNode sceneContent;

    @Type(type = "jsonb")
    @NotNull
    @ToString.Exclude
    private JsonNode metadataContent;
}
