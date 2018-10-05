package pl.cyfronet.s4e.granules;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.products.Product;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
public class Granule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Product product;
    private LocalDateTime timestamp;
    private String layerName;
}
