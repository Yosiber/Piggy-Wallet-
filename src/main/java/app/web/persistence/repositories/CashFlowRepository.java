package app.web.persistence.repositories;

import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlowEntity, Long> {
    List<CashFlowEntity> findByUserOrderByDateDesc(UserEntity user);
    List<CashFlowEntity> findByUserAndDateBetween(UserEntity user, LocalDateTime startDate, LocalDateTime endDate);
}