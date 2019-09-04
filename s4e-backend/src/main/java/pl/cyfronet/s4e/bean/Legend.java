package pl.cyfronet.s4e.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Legend implements Serializable {
    private String type;
    private String url;
    private Map<String,String> leftDescription;
    private Map<String,String> rightDescription;
    private Map<String,String> topMetric;
    private Map<String,String> bottomMetric;
}
