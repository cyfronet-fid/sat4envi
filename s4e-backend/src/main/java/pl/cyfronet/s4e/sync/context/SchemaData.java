package pl.cyfronet.s4e.sync.context;

import lombok.Value;
import org.leadpony.justify.api.JsonSchema;

@Value
public class SchemaData {
    String name;
    JsonSchema jsonSchema;
}
