package app.web.Service;

import app.web.persistence.entities.UserEntity;

import java.util.Optional;

public interface UserService {


    void createUser(UserEntity user);

    UserEntity saveUser(UserEntity user);

    UserEntity getUserById(Long id);
    UserEntity getUserByUsername(String username);

    UserEntity getCurrentSession();
}
