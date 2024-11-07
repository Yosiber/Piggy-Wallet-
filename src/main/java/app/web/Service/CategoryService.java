package app.web.Service;

import app.web.persistence.entities.CategoryEntity;

import java.util.Set;

public interface CategoryService {



    CategoryEntity createCategory(CategoryEntity category);
    Set<CategoryEntity> getCategories();
}
