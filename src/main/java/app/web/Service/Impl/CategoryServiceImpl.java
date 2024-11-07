package app.web.Service.Impl;

import app.web.Service.CategoryService;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        // Guarda y retorna la categoría guardada
        return categoryRepository.save(category);
    }

    @Override
    public Set<CategoryEntity> getCategories() {
        return new HashSet<>(categoryRepository.findAll());
    }

}
