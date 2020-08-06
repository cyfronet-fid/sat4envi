package pl.cyfronet.s4e;

import pl.cyfronet.s4e.bean.Schema;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaTestHelper {
    public static final String SCHEMA_PATH_PREFIX = "classpath:schema/";
    public static final List<String> SCENE_AND_METADATA_SCHEMA_NAMES = Stream.of(
            "Sentinel-1.scene.v1.json",
            "Sentinel-1.metadata.v1.json"
    )
            .map(s -> SCHEMA_PATH_PREFIX + s)
            .collect(Collectors.toList());

    public static Schema.SchemaBuilder schemaBuilder(String path, TestResourceHelper testResourceHelper) {
        String content = new String(testResourceHelper.getAsBytes(path));
        return Schema.builder()
                .name(path.substring(path.lastIndexOf("/") + 1))
                .type(path.contains("scene") ? Schema.Type.SCENE : Schema.Type.METADATA)
                .content(content);
    }
}
