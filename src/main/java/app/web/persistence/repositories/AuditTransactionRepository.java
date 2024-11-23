package app.web.persistence.repositories;

import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTransactionRepository extends JpaRepository<AuditTransactionEntity, Integer> {
}
