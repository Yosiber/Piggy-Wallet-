package app.web.listener;

import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditUserEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.repositories.AuditCategoryRepository;
import app.web.persistence.repositories.AuditUserRepository;
import jakarta.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Listener que audita las operaciones realizadas sobre entidades de categoría.
 * Se utiliza para registrar automáticamente la información de auditoría antes de la persistencia.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AuditCategoryListener {

    /** Repositorio para guardar registros de auditoría de categorías. */
    private final AuditCategoryRepository auditCategoryRepository;

    /**
     * Método ejecutado antes de la persistencia de una entidad {@link CategoryEntity}.
     * Registra un historial de la operación de inserción con información relevante.
     *
     * @param category la entidad de categoría que será persistida.
     */
    @PrePersist
    private void prePersist(CategoryEntity category) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        AuditCategoryEntity history = new AuditCategoryEntity();
        history.setName(category.getName());
        history.setDate(LocalDateTime.now());
        history.setIncome(category.isIncome());
        history.setOperation("INSERT");
        history.setUsername(username);
        this.auditCategoryRepository.save(history);
    }
}
