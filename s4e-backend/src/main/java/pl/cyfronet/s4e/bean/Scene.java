package pl.cyfronet.s4e.bean;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.hibernate.annotations.Type;
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scene extends CreationAndModificationAudited {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    /// E.g. "path/to/granule.tiff", excluding endpoint and bucket information
    @NotEmpty
    private String sceneKey;

    @NotNull
    private LocalDateTime timestamp;

    /// E.g. "path/to/granule.tiff", excluding endpoint and bucket information
    @NotEmpty
    private String s3Path;

    /// E.g. "mailto://s4e-sth/path/to/granule.tiff". Including endpoint and bucket information.
    @NotEmpty
    private String granulePath;

    @NotNull
    private Geometry footprint;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @ToString.Exclude
    private Legend legend;

    @Type(type = "jsonb")
    @ToString.Exclude
    private JsonNode sceneContent;

    @Type(type = "jsonb")
    @ToString.Exclude
    private JsonNode metadataContent;
}
