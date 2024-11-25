package app.web.service;

import app.web.persistence.entities.AuditUserEntity;
import app.web.persistence.entities.RoleEntity;
import app.web.persistence.entities.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servicio que gestiona las operaciones relacionadas con los usuarios.
 */
public interface UserService {

    /**
     * Obtiene una lista de todos los usuarios registrados.
     *
     * @return una lista de entidades de usuario.
     */
    List<UserEntity> getAllUsers();

    /**
     * Guarda un usuario en la base de datos.
     *
     * @param user la entidad del usuario a guardar.
     * @return la entidad del usuario guardada.
     */
    UserEntity saveUser(UserEntity user);

    /**
     * Busca un usuario por su nombre de usuario (username).
     *
     * @param username el nombre de usuario a buscar.
     * @return la entidad del usuario encontrada.
     */
    UserEntity getUserByUsername(String username);

    /**
     * Obtiene la información del usuario de la sesión actual.
     *
     * @return la entidad del usuario de la sesión actual.
     * @throws RuntimeException si no hay una sesión activa.
     */
    UserEntity getCurrentSession();

    /**
     * Cuenta el número total de usuarios registrados en el sistema.
     *
     * @return el número total de usuarios.
     */
    long countAllUsers();

    /**
     * Cuenta el número de usuarios actualmente activos (en sesión).
     *
     * @return el número de usuarios activos.
     */
    long countActiveUsers();

    /**
     * Obtiene una lista de los usuarios más recientes registrados en el sistema.
     *
     * @param limit el número máximo de usuarios a devolver.
     * @return una lista de las entidades de usuario más recientes.
     */
    List<UserEntity> getRecentUsers(int limit);

    /**
     * Obtiene un mapa que muestra el número de registros mensuales de usuarios.
     *
     * @return un mapa donde las claves son meses en formato "MMM yyyy" y los valores son la cantidad de registros.
     */
    Map<String, Long> getMonthlySignups();

    /**
     * Obtiene la distribución de usuarios por roles en el sistema.
     *
     * @return un mapa donde las claves son nombres de roles y los valores son la cantidad de usuarios por rol.
     */
    Map<String, Long> getRoleDistribution();

    /**
     * Elimina un usuario del sistema por su ID.
     *
     * @param id el ID del usuario a eliminar.
     */
    void deleteUser(Long id);

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param user la entidad del usuario a crear.
     * @throws RuntimeException si el rol predeterminado no se encuentra o si el usuario ya está registrado.
     */
    void createUser(UserEntity user);

    /**
     * Verifica si un nombre de usuario (username) ya existe en el sistema.
     *
     * @param username el nombre de usuario a verificar.
     * @return true si el nombre de usuario ya existe, false en caso contrario.
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si un correo electrónico ya existe en el sistema.
     *
     * @param email el correo electrónico a verificar.
     * @return true si el correo electrónico ya existe, false en caso contrario.
     */
    boolean existsByEmail(String email);
}