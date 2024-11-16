package app.web.persistence.repositories;

import app.web.persistence.entities.UpcomingPaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpcomingPaymentsRepository extends JpaRepository<UpcomingPaymentsEntity, Long> {
}
