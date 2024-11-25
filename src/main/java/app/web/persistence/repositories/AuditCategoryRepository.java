package app.web.persistence.repositories;

import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar operaciones de persistencia relacionadas con la auditoría de categorías.
 *
 * <p>Permite realizar operaciones CRUD sobre la entidad `AuditCategoryEntity`,
 * que almacena el historial de cambios realizados en las categorías.</p>
 *
 * @see AuditCategoryEntity
 * @author TuNombre
 */
@Repository
public interface AuditCategoryRepository extends JpaRepository<AuditCategoryEntity, Integer> {
}