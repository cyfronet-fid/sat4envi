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
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    private String name;
    @OneToMany(mappedBy = "product")
    private List<Scene> scenes;
    private String description;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Legend legend;
}
