package pl.cyfronet.s4e.service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.schema.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemaService {
    @Data
    @Builder
    public static class DTO {
        private String name;
        private Schema.Type type;
        private String content;
        private String previous;
    }

    private final SchemaRepository schemaRepository;

    private final ProductRepository productRepository;

    public <T> List<T> findAllBy(Class<T> projection) {
        return schemaRepository.findAllBy(projection, Sort.by(Sort.Order.asc("name")));
    }

    public String getContentByName(String name) throws NotFoundException {
        if (!schemaRepository.existsByName(name)) {
            throw constructNFE(name);
        }
        return schemaRepository.getContentByName(name);
    }

    public <T> T findByName(String name, Class<T> projection) throws NotFoundException {
        return schemaRepository.findByName(name, projection)
                .orElseThrow(() -> constructNFE(name));
    }

    @Transactional
    public Long create(DTO dto) throws NotFoundException, SchemaTypeException {
        Schema.SchemaBuilder schemaBuilder = Schema.builder()
                .name(dto.getName())
                .type(dto.getType())
                .content(dto.getContent());

        if (dto.getPrevious() != null) {
            schemaBuilder.previous(getValidatedPreviousSchema(dto.getType(), dto.getPrevious()));
        }

        Schema schema = schemaRepository.save(schemaBuilder.build());
        return schema.getId();
    }

    @Transactional
    public Long update(DTO dto) throws NotFoundException, SchemaTypeException {
        String name = dto.getName();
        Schema schema = schemaRepository.findByName(name)
                .orElseThrow(() -> constructNFE(name));

        if (dto.getType() != null) {
            schema.setType(dto.getType());
        }
        if (dto.getContent() != null) {
            schema.setContent(dto.getContent());
        }

        if (dto.getPrevious() != null) {
            String previousName = dto.getPrevious();
            if (previousName.isEmpty()) {
                schema.setPrevious(null);
            } else {
                schema.setPrevious(getValidatedPreviousSchema(schema.getType(), previousName));
            }
        }

        return schema.getId();
    }

    private Schema getValidatedPreviousSchema(Schema.Type expectedType, String previousName) throws NotFoundException, SchemaTypeException {
        Schema previous = schemaRepository.findByName(previousName)
                .orElseThrow(() -> constructNFE(previousName));

        if (previous.getType() != expectedType) {
            throw new SchemaTypeException("Type of schema must be the same as the previous");
        }

        return previous;
    }

    private interface DeleteProjection {
        Long getId();
    }

    @Transactional
    public void deleteByName(String name) throws NotFoundException, SchemaDeletionException {
        val optSchema = schemaRepository.findByName(name, DeleteProjection.class);
        if (!optSchema.isPresent()) {
            throw constructNFE(name);
        }
        Long schemaId = optSchema.get().getId();
        if (schemaRepository.existsByPreviousId(schemaId)) {
            throw new SchemaDeletionException("Another Schema references this Schema");
        }
        if (productRepository.existsBySceneSchemaId(schemaId) || productRepository.existsByMetadataSchemaId(schemaId)) {
            throw new SchemaDeletionException("Another Product references this Schema");
        }
        schemaRepository.deleteById(schemaId);
    }

    private NotFoundException constructNFE(String name) {
        return new NotFoundException("Schema not found for name '" + name + "'");
    }
}
