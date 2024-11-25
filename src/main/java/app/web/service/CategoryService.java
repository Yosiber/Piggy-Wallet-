package app.web.service;

import app.web.persistence.entities.CategoryEntity;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;
import java.util.Set;

/**
 * Servicio que gestiona las operaciones relacionadas con las categorías.
 */
public interface CategoryService {

    /**
     * Obtiene todas las categorías asociadas a un usuario.
     *
     * @param user el usuario cuyas categorías se van a recuperar.
     * @return un conjunto de categorías asociadas al usuario.
     */
    Set<CategoryEntity> getCategoriesByUser(User user);

    /**
     * Crea una nueva categoría asociada a un usuario.
     *
     * @param category la entidad de categoría a crear.
     * @param user el usuario al que pertenece la categoría.
     * @return la categoría creada.
     */
    CategoryEntity createCategory(CategoryEntity category, User user);

    /**
     * Busca una categoría por su ID.
     *
     * @param id el ID de la categoría.
     * @return un `Optional` que contiene la categoría encontrada, si existe.
     */
    Optional<CategoryEntity> findById(Long id);

    /**
     * Cuenta el número total de categorías registradas.
     *
     * @return el número total de categorías.
     */
    long countAllCategories();
}
