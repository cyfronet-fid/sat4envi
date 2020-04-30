package pl.cyfronet.s4e.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

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

    @NotEmpty
    private String name;

    /**
     * Must be set to match with the featureTypes in the PRG zip. It will also be the layer name
     */
    private String featureType;

    @ManyToOne
    private SldStyle sldStyle;

    private boolean created;
}
