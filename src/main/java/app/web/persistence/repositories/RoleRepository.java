package app.web.persistence.repositories;

import app.web.persistence.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}


