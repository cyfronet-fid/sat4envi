package pl.cyfronet.s4e.bean;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Entity
@Data
@Builder
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
public class ProductType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    private String name;
    @OneToMany(mappedBy = "productType")
    private List<Product> products;
    private String description;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Legend legend;
}
