package app.web.persistence.repositories;

import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface UpcomingPaymentsRepository extends JpaRepository<UpcomingPaymentsEntity, Long> {
    Set<UpcomingPaymentsEntity> findByUserUsername(String Name);

}
