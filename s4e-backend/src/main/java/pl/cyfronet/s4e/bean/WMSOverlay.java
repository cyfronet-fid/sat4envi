package pl.cyfronet.s4e.bean;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class WMSOverlay extends Overlay {
    private String url;
}