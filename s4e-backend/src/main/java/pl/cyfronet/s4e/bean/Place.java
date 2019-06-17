package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String type;
    @NotNull
    private double latitude;
    @NotNull
    private double longitude;
    @NotEmpty
    private String voivodeship;
}
