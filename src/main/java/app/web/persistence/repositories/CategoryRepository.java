package app.web.persistence.repositories;


import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.Set;


/**
 * Repositorio para la gestión de categorías.
 * Proporciona métodos para realizar operaciones CRUD sobre entidades de categoría.
 */
@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    /**
     * Encuentra todas las categorías asociadas al nombre de usuario de un usuario.
     *
     * @param username el nombre de usuario del propietario de las categorías.
     * @return un conjunto de entidades de categoría asociadas al usuario.
     */
    Set<CategoryEntity> findByUserUsername(String username);
}