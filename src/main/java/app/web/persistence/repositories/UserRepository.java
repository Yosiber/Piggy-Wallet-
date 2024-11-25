package app.web.persistence.repositories;


import app.web.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Repositorio para la gestión de usuarios.
 * Proporciona métodos para realizar operaciones CRUD y consultas específicas sobre entidades de usuario.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username el nombre de usuario a buscar.
     * @return un Optional que contiene el usuario encontrado, o vacío si no existe.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username")
    Optional<UserEntity> findByUsername(@Param("username") String username);

    /**
     * Encuentra los 5 usuarios más recientes ordenados por la fecha de creación en orden descendente.
     *
     * @return una lista con los 5 usuarios más recientes.
     */
    List<UserEntity> findTop5ByOrderByCreatedAtDesc();

    /**
     * Verifica si ya existe un usuario con el nombre de usuario especificado.
     *
     * @param username el nombre de usuario a verificar.
     * @return true si el nombre de usuario ya existe, false en caso contrario.
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si ya existe un usuario con el correo electrónico especificado.
     *
     * @param email el correo electrónico a verificar.
     * @return true si el correo electrónico ya existe, false en caso contrario.
     */
    boolean existsByEmail(String email);
}
