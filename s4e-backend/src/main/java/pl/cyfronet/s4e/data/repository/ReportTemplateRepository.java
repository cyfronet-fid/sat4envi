package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.ReportTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
public interface ReportTemplateRepository extends CrudRepository<ReportTemplate, UUID> {
    <T> Optional<T> findById(UUID id, Class<T> projection);

    @Query("SELECT rt " +
            "FROM ReportTemplate rt " +
            "JOIN rt.owner u " +
            "WHERE u.email = :email")
    <T> List<T> findAllByOwnerEmail(String email, Sort sort, Class<T> projection);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "JOIN ReportTemplate rt ON rt.owner.id = u.id " +
            "WHERE rt.id = :id")
    <T> Optional<T> findOwnerOf(UUID id, Class<T> projection);

    @Query("SELECT rt FROM ReportTemplate rt")
    @EntityGraph(attributePaths = "owner")
    <T> List<T> findAllFetchOwnerBy(Class<T> projection);
}
