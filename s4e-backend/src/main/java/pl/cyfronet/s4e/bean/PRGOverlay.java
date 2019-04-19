package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Data
@NoArgsConstructor
public class PRGOverlay extends Overlay {
    @Builder
    public PRGOverlay(String name, String featureType, SldStyle sldStyle, boolean created) {
        super();
        this.setName(name);
        this.setFeatureType(featureType);
        this.setSldStyle(sldStyle);
        this.setCreated(created);
    }

    /**
     * Must be set to match with the featureTypes in the PRG zip. It will also be the layer name
     */
    private String featureType;
    @ManyToOne
    private SldStyle sldStyle;
    private boolean created;
}
