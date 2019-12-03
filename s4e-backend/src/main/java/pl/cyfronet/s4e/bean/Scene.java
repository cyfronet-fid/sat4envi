package pl.cyfronet.s4e.bean;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
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
    /// How the layer will be identified in GeoServer, excluding workspace
    private String layerName;
    /// Has the layer, store and coverage been created for this product
    private boolean created;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Legend legend;
}
