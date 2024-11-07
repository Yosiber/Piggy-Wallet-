package app.web.persistence.repositories;

import app.web.persistence.entities.CashFlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlowEntity, Long> {
}
