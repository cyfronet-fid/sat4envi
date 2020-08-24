package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.OverlayOwner;
import pl.cyfronet.s4e.bean.WMSOverlay;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface WMSOverlayRepository extends CrudRepository<WMSOverlay, Long> {
    <T> Optional<T> findByOwnerTypeAndId(OverlayOwner owner, Long id, Class<T> projection);
    <T> Optional<T> findByOwnerTypeAndIdAndInstitutionId(OverlayOwner owner, Long id, Long institutionId, Class<T> projection);
    <T> Optional<T> findByOwnerTypeAndIdAndAppUserId(OverlayOwner owner, Long id, Long appUserId, Class<T> projection);

    <T> List<T> findAllByOwnerType(OverlayOwner owner, Class<T> projection);
    <T> List<T> findAllByOwnerTypeAndInstitutionId(OverlayOwner owner, Long institutionId, Class<T> projection);
    <T> List<T> findAllByOwnerTypeAndAppUserId(OverlayOwner owner, Long appUserId, Class<T> projection);
}
