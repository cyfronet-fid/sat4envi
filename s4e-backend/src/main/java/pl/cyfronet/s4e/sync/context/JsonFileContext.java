package pl.cyfronet.s4e.sync.context;

import lombok.Data;

import javax.json.JsonObject;

@Data
public class JsonFileContext {
    private String key;
    private String content;
    private SchemaData schema;
    private JsonObject json;
}
