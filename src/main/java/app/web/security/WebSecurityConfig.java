package app.web.security;
import app.web.service.Impl.UserDetailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


/**
 * Clase de configuración para la seguridad web.
 * Configura la autenticación, autorización, CSRF y manejo de inicio y cierre de sesión.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad para definir las políticas de acceso.
     *
     * @param http el objeto {@link HttpSecurity} para construir la configuración.
     * @return la cadena de filtros de seguridad configurada.
     * @throws Exception si ocurre algún error al construir la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeRequests((request) -> request
                        .requestMatchers("/finance/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/access")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Crea un codificador de contraseñas basado en BCrypt.
     *
     * @return una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura un proveedor de autenticación basado en DAO.
     *
     * @param userDetailsService el servicio de detalles de usuario personalizado.
     * @return una instancia de {@link AuthenticationProvider}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImpl userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    /**
     * Proporciona el gestor de autenticación configurado.
     *
     * @param authenticationConfiguration la configuración de autenticación.
     * @return una instancia de {@link AuthenticationManager}.
     * @throws Exception si ocurre algún error al obtener el gestor de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}