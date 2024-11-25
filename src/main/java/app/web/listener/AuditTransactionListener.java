package app.web.listener;

import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditTransactionEntity;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.repositories.AuditCategoryRepository;
import app.web.persistence.repositories.AuditTransactionRepository;
import jakarta.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Clase que actúa como un listener para auditar transacciones cuando se realiza
 * una operación de persistencia en la base de datos. Se asegura de registrar
 * información relevante sobre la transacción en la tabla de auditoría.
 *
 * <p>Utiliza la anotación {@link @PrePersist} para interceptar operaciones de persistencia
 * realizadas en entidades de tipo {@code CashFlowEntity}.</p>
 *
 * <p>Los repositorios de auditoría son inyectados mediante constructor gracias
 * a la anotación {@code @RequiredArgsConstructor}. La anotación {@code @Lazy}
 * garantiza que la inicialización se realice solo cuando sea necesario.</p>
 *
 * @author TuNombre
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AuditTransactionListener {

    /**
     * Repositorio para acceder a la categoría de auditoría.
     */
    private final AuditCategoryRepository auditCategoryRepository;

    /**
     * Repositorio para guardar registros de auditoría de transacciones.
     */
    private final AuditTransactionRepository auditTransactionRepository;

    /**
     * Método ejecutado antes de persistir una entidad de tipo {@code CashFlowEntity}.
     * Este método captura detalles de la transacción y los guarda en el registro de auditoría.
     *
     * @param transaction la entidad {@code CashFlowEntity} que se va a persistir
     */
    @PrePersist
    private void prePersist(CashFlowEntity transaction) {
        // Obtiene el nombre de usuario del contexto de seguridad actual
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Crea una nueva entidad de auditoría con los detalles de la transacción
        AuditTransactionEntity history = new AuditTransactionEntity();
        history.setAmount(transaction.getValue());
        history.setDate(LocalDateTime.now());
        history.setCategory(transaction.getCategory().getName());
        history.setOperation("INSERT");
        history.setUsername(username);

        // Guarda la entidad de auditoría en el repositorio
        this.auditTransactionRepository.save(history);
    }
}
