package pl.cyfronet.s4e.bean;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class PRGOverlay extends Overlay {
    @ManyToOne
    private SldStyle sldStyle;
}