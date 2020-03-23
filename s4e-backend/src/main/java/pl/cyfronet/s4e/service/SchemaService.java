package pl.cyfronet.s4e.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.SchemaCreationException;
import pl.cyfronet.s4e.ex.SchemaDeletionException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemaService {
    @Value
    @Builder
    public static class Create {
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
    public Long create(Create params) throws NotFoundException, SchemaCreationException {
        Schema.SchemaBuilder schemaBuilder = Schema.builder()
                .name(params.getName())
                .type(params.getType())
                .content(params.getContent());

        if (params.getPrevious() != null) {
            String previousName = params.getPrevious();
            Schema previous = schemaRepository.findByName(previousName)
                    .orElseThrow(() -> constructNFE(previousName));

            if (previous.getType() != params.getType()) {
                throw new SchemaCreationException("Type of created schema must be the same as the previous");
            }

            schemaBuilder.previous(previous);
        }

        Schema schema = schemaRepository.save(schemaBuilder.build());
        return schema.getId();
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
