package app.web.Service;

import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;
import java.util.Set;

public interface CategoryService {
    Set<CategoryEntity> getCategoriesByUser(User user);
    CategoryEntity createCategory(CategoryEntity category, User user);

    // Agregar este método para buscar una categoría por su ID
    Optional<CategoryEntity> findById(Long id);
}
