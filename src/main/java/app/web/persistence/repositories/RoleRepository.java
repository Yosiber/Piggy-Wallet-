package app.web.persistence.repositories;

import app.web.persistence.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de roles.
 * Proporciona métodos para realizar operaciones CRUD sobre entidades de rol.
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}

