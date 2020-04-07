package pl.cyfronet.s4e.sync.context;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class SceneJsonFileContext extends JsonFileContext {
    private Map<String, String> artifacts;
}
