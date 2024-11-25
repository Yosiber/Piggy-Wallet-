package app.web.persistence.repositories;

import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Repositorio para la gestión de flujos de efectivo (CashFlow).
 * Proporciona métodos para realizar operaciones CRUD sobre entidades de flujo de efectivo.
 */
@Repository
public interface CashFlowRepository extends JpaRepository<CashFlowEntity, Long> {

    /**
     * Encuentra todos los flujos de efectivo de un usuario y los ordena por fecha en orden descendente.
     *
     * @param user el usuario cuyo flujo de efectivo se quiere obtener.
     * @return una lista de entidades de flujo de efectivo ordenadas por fecha en orden descendente.
     */
    List<CashFlowEntity> findByUserOrderByDateDesc(UserEntity user);
}