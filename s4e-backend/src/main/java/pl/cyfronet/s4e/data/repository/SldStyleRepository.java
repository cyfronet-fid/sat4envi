package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.SldStyle;

@Transactional(readOnly = true)
public interface SldStyleRepository extends CrudRepository<SldStyle, Long> {
}
