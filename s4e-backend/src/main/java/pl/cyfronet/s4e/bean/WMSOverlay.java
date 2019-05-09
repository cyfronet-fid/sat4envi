package pl.cyfronet.s4e.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class WMSOverlay extends Overlay {
    private String url;
}
