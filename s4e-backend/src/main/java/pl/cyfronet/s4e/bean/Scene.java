package pl.cyfronet.s4e.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.locationtech.jts.geom.Geometry;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * There is ON CASCADE DELETE on Product
 */
@Entity
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
