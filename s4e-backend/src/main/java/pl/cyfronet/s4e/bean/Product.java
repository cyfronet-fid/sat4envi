package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private ProductType productType;
    @NotNull
    private LocalDateTime timestamp;
    /// E.g. "path/to/granule.tiff", excluding endpoint and bucket information
    @NotEmpty
    private String s3Path;
    /// How the layer will be identified in GeoServer, excluding workspace
    private String layerName;
    /// Has the layer, store and coverage been created for this product
    private boolean created;
}
