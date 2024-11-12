package app.web.persistence.repositories;


import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.Set;


@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Set<CategoryEntity> findByUserUsername(String catName);
    Set<CategoryEntity> findByUser(UserEntity user);
}
