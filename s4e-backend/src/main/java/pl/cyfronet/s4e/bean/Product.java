package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.ProductType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private ProductType productType;
    private LocalDateTime timestamp;
    private String layerName;
}
