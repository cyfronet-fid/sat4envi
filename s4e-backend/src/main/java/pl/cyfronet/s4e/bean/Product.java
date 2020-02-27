package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
 import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * If you delete Product, you will also delete all Scene entries
 */
@Entity
@Data
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String displayName;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    private List<Scene> scenes;

    private String description;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @ToString.Exclude
    private Legend legend;
    /// How the layer will be identified in GeoServer, excluding workspace
    private String layerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Schema sceneSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Schema metadataSchema;

    private String granuleArtifact;

    @Type(type = "jsonb")
    @ToString.Exclude
    private Map<String, Object> searchableMetadata;
}
