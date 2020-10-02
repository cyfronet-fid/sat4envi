package pl.cyfronet.s4e.sync.context;

import jakarta.json.JsonObject;
import lombok.Data;

@Data
public class JsonFileContext {
    private String key;
    private String content;
    private SchemaData schema;
    private JsonObject json;
}
