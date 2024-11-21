package app.web.Service;

import app.web.persistence.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserEntity> getAllUsers();
    UserEntity saveUser(UserEntity user);
    UserEntity getUserById(Long id);
    UserEntity getUserByUsername(String username);
    UserEntity getCurrentSession();
    void deleteUser(Long id);
    void createUser(UserEntity user);
}
