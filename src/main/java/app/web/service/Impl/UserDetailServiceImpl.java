package app.web.service.Impl;


import app.web.service.UserService;
import app.web.persistence.entities.RoleEntity;
import app.web.persistence.entities.UserEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementación de {@link UserDetailsService} para gestionar la autenticación de usuarios.
 * Esta clase se integra con Spring Security y proporciona detalles de usuario para la autenticación.
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;

    /**
     * Carga los detalles de un usuario basado en su nombre de usuario.
     *
     * @param username el nombre de usuario del usuario que se desea cargar.
     * @return una instancia de {@link UserDetails} con la información del usuario para la autenticación.
     * @throws UsernameNotFoundException si no se encuentra un usuario con el nombre de usuario proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.getUserByUsername(username);

        if (user != null) {
            List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
            return buildUserForAuthentication(user, authorities);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    /**
     * Convierte los roles del usuario en una lista de autoridades reconocidas por Spring Security.
     *
     * @param userRoles un conjunto de roles del usuario.
     * @return una lista de {@link GrantedAuthority} que representan los roles del usuario.
     */
    private List<GrantedAuthority> getUserAuthority(Set<RoleEntity> userRoles) {
        Set<GrantedAuthority> roles = new HashSet<>();
        for (RoleEntity role : userRoles) {
            roles.add(new SimpleGrantedAuthority(role.getRolName()));
        }
        return new ArrayList<>(roles);
    }

    /**
     * Construye un objeto de autenticación de usuario basado en la información del usuario y sus roles.
     *
     * @param user una entidad {@link UserEntity} que contiene los detalles del usuario.
     * @param authorities una lista de autoridades asignadas al usuario.
     * @return una instancia de {@link UserDetails} configurada para la autenticación.
     */
    private UserDetails buildUserForAuthentication(UserEntity user, List<GrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, // habilitado
                true, // cuenta no expirada
                true, // credenciales no expiradas
                true, // cuenta no bloqueada
                authorities
        );
    }
}
