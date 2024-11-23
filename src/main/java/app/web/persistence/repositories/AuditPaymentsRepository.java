package app.web.persistence.repositories;

import app.web.persistence.entities.AuditPaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditPaymentsRepository extends JpaRepository <AuditPaymentsEntity, Integer> {
}
