package pl.cyfronet.s4e.db.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.util.ResourceReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class SchemaScanner {
    private final ResourceLoader resourceLoader;

    public List<Schema> scan(String path) throws IOException {
        log.trace(String.format("Scanning: '%s'", path));
        Resource resource = resourceLoader.getResource(path);
        File file = resource.getFile();
        List<Schema> schemas = new ArrayList<>();
        if (file.isDirectory()) {
            for (String child : file.list()) {
                schemas.addAll(scan(path + "/" + child));
            }
        } else {
            String content = ResourceReader.asString(resource);
            schemas.add(loadSchema(content));
        }
        return schemas;
    }

    private Schema loadSchema(String content) {
        JsonObject jsonObject;
        try (JsonReader reader = Json.createReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))) {
            jsonObject = reader.readObject();
        }
        String name = jsonObject.getString("id");
        Schema.Type type = name.contains("scene") ? Schema.Type.SCENE : Schema.Type.METADATA;
        return Schema.builder()
                .name(name)
                .content(content)
                .type(type)
                .build();
    }
}
