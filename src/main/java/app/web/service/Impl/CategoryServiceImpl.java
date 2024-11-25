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

/**
 * Implementación del servicio para la gestión de categorías.
 * Proporciona métodos para crear, eliminar, contar y recuperar categorías asociadas a un usuario.
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * Cuenta el número total de categorías en el sistema.
     *
     * @return el número total de categorías.
     */
    @Override
    public long countAllCategories() {
        return categoryRepository.count();
    }

    /**
     * Obtiene las categorías asociadas a un usuario específico.
     *
     * @param user el usuario cuyas categorías se desean obtener.
     * @return un conjunto de categorías asociadas al usuario.
     */
    @Override
    public Set<CategoryEntity> getCategoriesByUser(User user) {
        return categoryRepository.findByUserUsername(user.getUsername());
    }

    /**
     * Crea una nueva categoría asociada a un usuario específico.
     *
     * @param category la categoría a crear.
     * @param user el usuario al que se asociará la categoría.
     * @return la categoría creada y almacenada en la base de datos.
     * @throws RuntimeException si el usuario no se encuentra en la base de datos.
     */
    @Override
    public CategoryEntity createCategory(CategoryEntity category, User user) {
        UserEntity userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        category.setUser(userEntity);

        return categoryRepository.save(category);
    }

    /**
     * Busca una categoría por su ID.
     *
     * @param id el ID de la categoría a buscar.
     * @return un Optional que contiene la categoría encontrada, o vacío si no existe.
     */
    @Override
    public Optional<CategoryEntity> findById(Long id) {
        return categoryRepository.findById(id);
    }
}

