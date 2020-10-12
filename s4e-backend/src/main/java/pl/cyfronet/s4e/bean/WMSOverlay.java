package pl.cyfronet.s4e.bean;

import lombok.*;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "wms_overlay")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WMSOverlay extends CreationAndModificationAudited {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String label;

    @NotEmpty
    private String layerName;

    @NotNull
    private String url;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OverlayOwner ownerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Include
    private AppUser appUser;
}
