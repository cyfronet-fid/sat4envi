package pl.cyfronet.s4e.products;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.granules.Granule;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany
    private List<Granule> granules;
}
