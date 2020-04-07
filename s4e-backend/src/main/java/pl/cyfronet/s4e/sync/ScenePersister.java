package pl.cyfronet.s4e.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.properties.S3Properties;

import javax.json.JsonObject;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenePersister {
    private final GeoServerProperties geoServerProperties;
    private final S3Properties s3Properties;

    private final ProductRepository productRepository;
    private final SceneRepository sceneRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long persist(Prototype prototype) throws NotFoundException {
        Product product = productRepository.findById(prototype.getProductId())
                .orElseThrow(() -> new NotFoundException("Product with id '" + prototype.getProductId() + "' not found"));

        String granulePath = geoServerProperties.getEndpoint() + "://" + s3Properties.getBucket() + "/" + prototype.getS3Path();

        JsonNode sceneJsonNode = convert(prototype.getSceneJson());
        JsonNode metadataJsonNode = convert(prototype.getMetadataJson());

        Scene scene = sceneRepository.save(Scene.builder()
                .product(product)
                .timestamp(prototype.getTimestamp())
                .s3Path(prototype.getS3Path())
                .granulePath(granulePath)
                .footprint(prototype.getFootprint())
                .sceneContent(sceneJsonNode)
                .metadataContent(metadataJsonNode)
                .build());

        return scene.getId();
    }

    private JsonNode convert(JsonObject sceneJson) {
        try {
            return objectMapper.readTree(sceneJson.toString());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
