package app.web.service;

import app.web.persistence.entities.AuditUserEntity;
import app.web.persistence.entities.RoleEntity;
import app.web.persistence.entities.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    List<UserEntity> getAllUsers();
    UserEntity saveUser(UserEntity user);
    UserEntity getUserById(Long id);
    UserEntity getUserByUsername(String username);
    UserEntity getCurrentSession();
    long countAllUsers();
    long countActiveUsers();
    List<UserEntity> getRecentUsers(int limit);
    Map<String, Long> getMonthlySignups();
    Map<String, Long> getRoleDistribution();
    void deleteUser(Long id);
    void createUser(UserEntity user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}