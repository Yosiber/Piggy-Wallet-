package app.web.persistence.repositories;

import app.web.listener.AuditTransactionListener;
import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditTransactionEntity;
import app.web.persistence.entities.CashFlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar operaciones de persistencia relacionadas con la auditoría de transacciones financieras.
 *
 * <p>Permite realizar operaciones CRUD sobre la entidad `AuditTransactionEntity`,
 * que almacena el historial de cambios realizados en las transacciones de flujo de caja.</p>
 *
 * @see AuditTransactionEntity
 * @see CashFlowEntity
 * @see AuditTransactionListener
 *
 * @author TuNombre
 */
@Repository
public interface AuditTransactionRepository extends JpaRepository<AuditTransactionEntity, Integer> {
}