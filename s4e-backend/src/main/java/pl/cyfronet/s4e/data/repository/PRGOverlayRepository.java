package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import pl.cyfronet.s4e.bean.PRGOverlay;

import java.util.List;

public interface PRGOverlayRepository extends CrudRepository<PRGOverlay, Long> {
    List<PRGOverlay> findAllByCreatedTrue();
}
