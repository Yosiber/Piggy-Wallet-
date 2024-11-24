package app.web.service.Impl;

import app.web.service.CategoryService;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.repositories.CategoryRepository;
import app.web.persistence.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {


    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public CategoryEntity deleteCategory(Long id, User user) {
        Optional<CategoryEntity> category = categoryRepository.findById(id);
        if (category.isPresent() && category.get().getUser().equals(user)) {
            CategoryEntity categoryToDelete = category.get();
            categoryRepository.deleteById(id);
            return categoryToDelete;
        }
        return null;
    }

    @Override
    public long countAllCategories() {
        return categoryRepository.count();
    }

    @Override
    public Set<CategoryEntity> getCategoriesByUser(User user) {
        Set<CategoryEntity> categories = categoryRepository.findByUserUsername(user.getUsername());
        return categories;
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category, User user) {
        UserEntity userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        category.setUser(userEntity);


        CategoryEntity savedCategory = categoryRepository.save(category);

        return savedCategory;
    }

    @Override
    public Optional<CategoryEntity> findById(Long id) {
        return categoryRepository.findById(id);
    }
}
