package app.web.service;

import app.web.persistence.entities.CategoryEntity;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;
import java.util.Set;

public interface CategoryService {
    Set<CategoryEntity> getCategoriesByUser(User user);
    CategoryEntity createCategory(CategoryEntity category, User user);
    Optional<CategoryEntity> findById(Long id);
    long countAllCategories();
}
