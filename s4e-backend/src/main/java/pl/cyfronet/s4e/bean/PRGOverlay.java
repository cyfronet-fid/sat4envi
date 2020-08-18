package pl.cyfronet.s4e.bean;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "prg_overlay")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PRGOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Must be set to match with the featureTypes in the PRG zip. It will also be the layer name
     */
    private String featureType;

    @ManyToOne
    private SldStyle sldStyle;

    private boolean created;

    @OneToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private WMSOverlay wmsOverlay;
}
