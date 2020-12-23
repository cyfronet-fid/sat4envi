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

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * If you delete Product, you will also delete all Scene entries
 */
@Entity
@DynamicUpdate
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Product extends CreationAndModificationAudited {
    public enum AccessType {
        OPEN, EUMETSAT, PRIVATE
    }

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

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private Set<LicenseGrant> licenseGrants = new HashSet<>();

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

    /**
     * A map, where key, a format value => an artifact key, which will be used as an ImageMosaic granule path.
     *
     * <p>
     * The format value can be for example <code>GeoTiff</code>, or <code>COG</code>.
     * <p>
     * If given format is not found in the keys, then the default key is taken.
     * It should be always defined.
     * <p>
     * For example, <code>{"default":"foo", "bar":"baz"}</code>.
     * If format is <code>bar</code>, then the granule path key will be <code>baz</code>.
     * When format is <code>foobar</code>, then a default granule path key <code>foo</code> will be taken.
     */
    @Type(type = "jsonb")
    @NotNull
    @ToString.Exclude
    private Map<String, String> granuleArtifactRule;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    @Builder.Default
    @ToString.Exclude
    private ProductCategory productCategory = ProductCategory.builder().id(1L).build();

    public void addFavourite(AppUser appUser) {
        this.favourites.add(appUser);
    }

    public void removeFavourite(AppUser appUser) {
        this.favourites.remove(appUser);
    }
}
