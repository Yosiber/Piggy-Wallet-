package app.web.persistence.repositories;

import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Repositorio para la gestión de pagos futuros (UpcomingPayments).
 * Proporciona métodos para realizar operaciones CRUD sobre entidades de pagos futuros.
 */
@Repository
public interface UpcomingPaymentsRepository extends JpaRepository<UpcomingPaymentsEntity, Long> {

    /**
     * Encuentra todos los pagos futuros asociados al nombre de usuario de un usuario.
     *
     * @param username el nombre de usuario del propietario de los pagos futuros.
     * @return un conjunto de entidades de pagos futuros asociadas al usuario.
     */
    Set<UpcomingPaymentsEntity> findByUserUsername(String username);
}
