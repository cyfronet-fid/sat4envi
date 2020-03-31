package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * There is ON CASCADE DELETE on Product
 */
@Entity
@Data
@Builder
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

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
    private Map<String, Object> sceneContent;

    @Type(type = "jsonb")
    @ToString.Exclude
    private Map<String, Object> metadataContent;
}
