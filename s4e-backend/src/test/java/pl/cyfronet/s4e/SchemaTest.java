package pl.cyfronet.s4e;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@BasicTest
@Slf4j
class SchemaTest {
    private static final String SCENE_SCHEMA_PATH =     "classpath:schema/Sentinel-1.scene.v1.json";
    private static final String METADATA_SCHEMA_PATH =  "classpath:schema/Sentinel-1.metadata.v1.json";
    private static final String SCENE_INVALID_PATH =    "classpath:schema/Sentinel-1.invalid.scene";
    private static final String SCENE_PATH =            "classpath:schema/S1A_IW_GRDH_1SDV_20200228T045117_20200228T045142_031448_039EDF_82C8.scene";
    private static final String METADATA_INVALID_PATH = "classpath:schema/Sentinel-1.invalid.metadata";
    private static final String METADATA_PATH =         "classpath:schema/S1A_IW_GRDH_1SDV_20200228T045117_20200228T045142_031448_039EDF_82C8.metadata";

    @Autowired
    private SchemaRepository schemaRepo;

    @Autowired
    private TestResourceHelper trh;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void test() throws IOException {
        JsonValidationService jvs = JsonValidationService.newInstance();
//        JsonProvider jsonProvider = ServiceLoader.load(JsonProvider.class).findFirst().get();

        // create schemas
        schemaRepo.save(Schema.builder()
                .name("Sentinel-1.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(new String(trh.getAsBytes(SCENE_SCHEMA_PATH)))
                .build());
        schemaRepo.save(Schema.builder()
                .name("Sentinel-1.metadata.v1.json")
                .type(Schema.Type.METADATA)
                .content(new String(trh.getAsBytes(METADATA_SCHEMA_PATH)))
                .build());

        ProblemHandler handler = jvs.createProblemPrinter(p -> {
            throw new IllegalArgumentException(p);
        });

        // validate scene file
        {
            String scenePath = SCENE_PATH;
            String schemaName;
            try (JsonReader jsonReader = Json.createReader(trh.getAsInputStream(scenePath))) {
                schemaName = jsonReader.readObject().getString("schema");
            }
            assertThat(schemaName, is(equalTo("Sentinel-1.scene.v1.json")));
            Schema schema = schemaRepo.findByName(schemaName).get();
            JsonSchema jsonSchema = jvs.readSchema(new ByteArrayInputStream(schema.getContent().getBytes(StandardCharsets.UTF_8)));

            try (JsonReader reader = jvs.createReader(trh.getAsInputStream(scenePath), StandardCharsets.UTF_8, jsonSchema, handler)) {
                JsonValue value = reader.readValue();
                log.info(value.toString());
            }

            try (JsonReader reader = jvs.createReader(trh.getAsInputStream(SCENE_INVALID_PATH), StandardCharsets.UTF_8, jsonSchema, handler)) {
                reader.read();
                fail("Should throw");
            } catch (Exception e) {
                log.info("Thrown", e);
            }
        }

        // validate metadata file
        {
            String metadataPath = METADATA_PATH;
            String schemaName;
            try (JsonReader jsonReader = Json.createReader(trh.getAsInputStream(metadataPath))) {
                schemaName = jsonReader.readObject().getString("schema");
            }
            assertThat(schemaName, is(equalTo("Sentinel-1.metadata.v1.json")));
            Schema schema = schemaRepo.findByName(schemaName).get();
            JsonSchema jsonSchema = jvs.readSchema(new ByteArrayInputStream(schema.getContent().getBytes(StandardCharsets.UTF_8)));

            try (JsonReader reader = jvs.createReader(trh.getAsInputStream(metadataPath), StandardCharsets.UTF_8, jsonSchema, handler)) {
                JsonValue value = reader.readValue();
                log.info(value.toString());
            }

            try (JsonReader reader = jvs.createReader(trh.getAsInputStream(METADATA_INVALID_PATH), StandardCharsets.UTF_8, jsonSchema, handler)) {
                reader.read();
                fail("Should throw");
            } catch (Exception e) {
                log.info("Thrown", e);
            }
        }
    }

    @Test
    public void validateTest() throws IOException {
        val jvs = JsonValidationService.newInstance();

        JsonSchema schema = jvs.readSchema(trh.getAsInputStream(SCENE_SCHEMA_PATH), StandardCharsets.UTF_8);

        ProblemHandler handler = jvs.createProblemPrinter(log::info);

        try (JsonReader reader = jvs.createReader(trh.getAsInputStream(SCENE_PATH), StandardCharsets.UTF_8, schema, handler)) {
            JsonObject jsonObject = reader.readObject();
            log.info(jsonObject.toString());
        }
    }
}
