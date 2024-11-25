package app.web.persistence.repositories;

import app.web.listener.AuditUserListener;
import app.web.persistence.entities.AuditUserEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar operaciones de persistencia relacionadas con la auditoría de usuarios.
 *
 * <p>Permite realizar operaciones CRUD sobre la entidad `AuditUserEntity`,
 * que almacena el historial de cambios realizados en los usuarios.</p>
 *
 * @see AuditUserEntity
 * @see UserEntity
 * @see AuditUserListener
 *
 * @author TuNombre
 */
@Repository
public interface AuditUserRepository extends JpaRepository<AuditUserEntity, Integer> {
}
