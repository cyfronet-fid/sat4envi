package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * If you delete Product, you will also delete all Scene entries
 */
@Entity
@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotEmpty
    @Pattern(regexp = "^[-a-zA-Z_0-9]+$")
    @EqualsAndHashCode.Include
    private String name;

    @NotEmpty
    @EqualsAndHashCode.Include
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
    @Pattern(regexp = "^[a-z_0-9]+$")
    @EqualsAndHashCode.Include
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

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "favourite_product_app_users",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "app_user_id")
    )
    @ToString.Exclude
    @Builder.Default
    private Set<AppUser> favourites = new HashSet<>();

    public void addFavourite(AppUser appUser) {
        this.favourites.add(appUser);
    }

    public void removeFavourite(AppUser appUser) {
        this.favourites.remove(appUser);
    }
}
