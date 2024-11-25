package app.web.listener;

import app.web.persistence.entities.AuditUserEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.repositories.AuditUserRepository;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Clase que actúa como un listener para auditar operaciones realizadas sobre la entidad {@code UserEntity}.
 * Este listener intercepta las operaciones de persistencia, actualización y eliminación para registrar
 * detalles relevantes en una tabla de auditoría.
 *
 * <p>Utiliza las anotaciones {@link @PrePersist}, {@link @PreUpdate} y {@link @PreRemove} para capturar
 * los eventos correspondientes de las operaciones en la base de datos.</p>
 *
 * @author TuNombre
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AuditUserListener {

    /**
     * Repositorio para guardar registros de auditoría relacionados con los usuarios.
     */
    private final AuditUserRepository auditUserRepository;

    /**
     * Método ejecutado antes de eliminar una entidad {@code UserEntity}.
     * Registra un evento de eliminación en la tabla de auditoría.
     *
     * @param user la entidad {@code UserEntity} que se va a eliminar.
     */
    @PreRemove
    private void preRemove(UserEntity user) {
        // Obtiene el nombre del usuario actual o "system" si no hay autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "system";

        // Crea un registro de auditoría para la operación de eliminación
        AuditUserEntity history = new AuditUserEntity();
        history.setName(user.getUsername());
        history.setDate(LocalDateTime.now());
        history.setOperation("DELETE");
        history.setUsername(username);

        // Guarda el registro en el repositorio
        this.auditUserRepository.save(history);
    }

    /**
     * Método ejecutado antes de actualizar una entidad {@code UserEntity}.
     * Registra un evento de actualización en la tabla de auditoría.
     *
     * @param user la entidad {@code UserEntity} que se va a actualizar.
     */
    @PreUpdate
    private void preUpdate(UserEntity user) {
        // Obtiene el nombre del usuario actual o "system" si no hay autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "system";

        // Crea un registro de auditoría para la operación de actualización
        AuditUserEntity history = new AuditUserEntity();
        history.setName(user.getUsername());
        history.setDate(LocalDateTime.now());
        history.setOperation("UPDATE");
        history.setUsername(username);

        // Guarda el registro en el repositorio
        this.auditUserRepository.save(history);
    }

    /**
     * Método ejecutado antes de persistir una entidad {@code UserEntity}.
     * Registra un evento de inserción en la tabla de auditoría.
     *
     * @param user la entidad {@code UserEntity} que se va a persistir.
     */
    @PrePersist
    private void prePersist(UserEntity user) {
        // Obtiene el nombre del usuario actual desde el contexto de seguridad
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Crea un registro de auditoría para la operación de inserción
        AuditUserEntity history = new AuditUserEntity();
        history.setName(user.getUsername());
        history.setDate(LocalDateTime.now());
        history.setOperation("INSERT");
        history.setUsername(username);

        // Guarda el registro en el repositorio
        this.auditUserRepository.save(history);
    }
}
