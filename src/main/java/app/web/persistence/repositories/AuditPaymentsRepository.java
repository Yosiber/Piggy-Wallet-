package app.web.persistence.repositories;

import app.web.persistence.entities.AuditPaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar operaciones de persistencia relacionadas con la auditoría de pagos próximos.
 *
 * <p>Permite realizar operaciones CRUD sobre la entidad `AuditPaymentsEntity`,
 * que almacena el historial de cambios realizados en los pagos próximos.</p>
 *
 * @see AuditPaymentsEntity
 * @author TuNombre
 */
public interface AuditPaymentsRepository extends JpaRepository<AuditPaymentsEntity, Integer> {
}