package app.web.Service.Impl;


import app.web.Service.UserService;
import app.web.persistence.entities.UserEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> Optionaluser = userService.getUserByUsername(username);

        if (Optionaluser.isPresent()) {
            session.setAttribute("user_session_id", Optionaluser.get().getId());
            UserEntity user = Optionaluser.get();
            return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles()
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
