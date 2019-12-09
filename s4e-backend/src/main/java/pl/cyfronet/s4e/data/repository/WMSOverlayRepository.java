package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.WMSOverlay;

@Transactional(readOnly = true)
public interface WMSOverlayRepository extends CrudRepository<WMSOverlay, Long> {
}
