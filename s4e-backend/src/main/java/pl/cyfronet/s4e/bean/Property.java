package pl.cyfronet.s4e.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    @Id
    private String name;

    private String value;
}
