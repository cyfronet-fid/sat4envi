package pl.cyfronet.s4e.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.LicenseGrant;

@Transactional(readOnly = true)
public interface LicenseGrantRepository extends CrudRepository<LicenseGrant, Long> {
}
