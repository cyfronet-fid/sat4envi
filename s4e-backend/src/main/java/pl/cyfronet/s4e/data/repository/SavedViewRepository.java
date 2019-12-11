package pl.cyfronet.s4e.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.SavedView;
import pl.cyfronet.s4e.controller.response.SavedViewResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
public interface SavedViewRepository extends CrudRepository<SavedView, UUID> {
    <T> List<T> findAllBy(Class<T> projection);
    <T> Optional<T> findById(UUID id, Class<T> projection);

    @Query("SELECT sv " +
            "FROM SavedView sv " +
            "JOIN sv.owner u " +
            "WHERE u.email = :email")
    Page<SavedViewResponse> findAllByOwnerEmail(String email, Pageable pageable);

    @Query("SELECT u " +
            "FROM AppUser u " +
            "JOIN SavedView sv ON sv.owner.id = u.id " +
            "WHERE sv.id = :id")
    <T> Optional<T> findOwnerOf(UUID id, Class<T> projection);
}
