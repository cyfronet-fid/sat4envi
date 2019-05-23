package pl.cyfronet.s4e.bean;

import lombok.Data;

import javax.persistence.*;

@MappedSuperclass
@Data
public class Overlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
}
