package app.web.Service;

import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryService {
    Set<CategoryEntity> getCategoriesByUser(User user);
    CategoryEntity createCategory(CategoryEntity category, User user);
    Optional<CategoryEntity> findById(Long id);
}
