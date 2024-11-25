package app.web.service.Impl;

import app.web.listener.ActiveUserSessionListener;
import app.web.persistence.entities.AuditUserEntity;
import app.web.service.UserService;
import app.web.persistence.entities.RoleEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.repositories.RoleRepository;
import app.web.persistence.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz {@link UserService} que gestiona las operaciones relacionadas con usuarios.
 * Incluye métodos para creación, consulta, actualización y eliminación de usuarios,
 * además de estadísticas como usuarios activos, registros mensuales y distribución de roles.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ActiveUserSessionListener activeUserSessionListener;

    /**
     * Crea un nuevo usuario con un rol por defecto y lo guarda en la base de datos.
     *
     * @param user la entidad del usuario que se va a crear.
     * @throws RuntimeException si el usuario ya está registrado o si ocurre algún error durante el proceso.
     */
    @Override
    public void createUser(UserEntity user) {
        try {
            RoleEntity roleDefault = roleRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));
            Set<RoleEntity> roles = new HashSet<>();
            roles.add(roleDefault);
            user.setRoles(roles);
            userRepository.save(user);

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Esta cuenta ya esta registrada");
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage());
        }
    }

    /**
     * Comprueba si existe un usuario con el nombre de usuario proporcionado.
     *
     * @param username el nombre de usuario a verificar.
     * @return {@code true} si el usuario existe, {@code false} en caso contrario.
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Comprueba si existe un usuario con el correo electrónico proporcionado.
     *
     * @param email el correo electrónico a verificar.
     * @return {@code true} si el correo existe, {@code false} en caso contrario.
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Cuenta el total de usuarios registrados.
     *
     * @return el número total de usuarios.
     */
    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    /**
     * Cuenta el total de usuarios activos basándose en sesiones activas.
     *
     * @return el número de usuarios activos.
     */
    @Override
    public long countActiveUsers() {
        return activeUserSessionListener.getActiveSessions();
    }

    /**
     * Obtiene una lista de los usuarios más recientes registrados, limitada al número especificado.
     *
     * @param limit el número máximo de usuarios recientes a devolver.
     * @return una lista de los usuarios más recientes.
     */
    @Override
    public List<UserEntity> getRecentUsers(int limit) {
        return userRepository.findTop5ByOrderByCreatedAtDesc();
    }

    /**
     * Obtiene un mapa de registros de usuarios agrupados por mes y año.
     *
     * @return un mapa donde las claves son meses/años y los valores el conteo de registros.
     */
    @Override
    public Map<String, Long> getMonthlySignups() {
        List<UserEntity> users = userRepository.findAll();
        Map<String, Long> monthlySignups = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        users.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().format(formatter),
                        Collectors.counting()
                ))
                .forEach(monthlySignups::put);

        return monthlySignups;
    }

    /**
     * Obtiene la distribución de roles entre los usuarios registrados.
     *
     * @return un mapa donde las claves son nombres de roles y los valores el conteo de usuarios con ese rol.
     */
    @Override
    public Map<String, Long> getRoleDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        List<UserEntity> users = userRepository.findAll();

        for (UserEntity user : users) {
            for (RoleEntity role : user.getRoles()) {
                distribution.merge(role.getRolName(), 1L, Long::sum);
            }
        }

        return distribution;
    }

    /**
     * Guarda o actualiza la información de un usuario en la base de datos.
     *
     * @param user la entidad del usuario a guardar.
     * @return el usuario guardado.
     */
    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username el nombre de usuario.
     * @return el usuario encontrado, o {@code null} si no existe.
     */
    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Obtiene el usuario asociado a la sesión actual.
     *
     * @return el usuario de la sesión actual.
     * @throws RuntimeException si no hay sesión activa.
     */
    @Override
    public UserEntity getCurrentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("No hay sesion activa");
        }

        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new RuntimeException("No hay sesion activa");
        }

        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }

    /**
     * Obtiene una lista de todos los usuarios registrados.
     *
     * @return una lista de usuarios.
     */
    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id el ID del usuario a eliminar.
     */
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
