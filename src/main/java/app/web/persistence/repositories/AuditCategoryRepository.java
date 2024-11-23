package app.web.persistence.repositories;

import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditCategoryRepository extends JpaRepository<AuditCategoryEntity, Integer> {
}
