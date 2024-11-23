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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public void createUser(UserEntity user) {
        try {
            RoleEntity roleDefault = roleRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));

            Set<RoleEntity> roles = new HashSet<>();
            roles.add(roleDefault);
            user.setRoles(roles);

            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage());
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }

    @Override
    public long countActiveUsers() {
        return activeUserSessionListener.getActiveSessions();
    }

    @Override
    public List<UserEntity> getRecentUsers(int limit) {
        return userRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    public Map<String, Long> getMonthlySignups() {
        List<UserEntity> users = userRepository.findAll();
        Map<String, Long> monthlySignups = new LinkedHashMap<>();

        // Procesar usuarios y agrupar por mes
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

    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public UserEntity getCurrentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("No hay sesion activa");
        }

        HttpSession session = request.getSession(false);

        if(session == null) {
            throw new RuntimeException("No hay sesion activa");
        }

        UserEntity user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
