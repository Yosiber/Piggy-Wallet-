package app.web.controller;

import app.web.service.UserService;
import app.web.persistence.entities.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    /**
     * Muestra la página de registro de usuarios.
     *
     * @return el nombre de la plantilla correspondiente a la página de registro ("/users/register").
     */
    @GetMapping("/register")
    public String recordPage() {
        return "/users/register";
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param user el objeto {@link UserEntity} que contiene los datos del usuario a registrar.
     * @return redirige a la página de inicio de sesión ("/login") después del registro exitoso.
     */
    @PostMapping("/register")
    public String register(UserEntity user, Model model) {
        try {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            userService.createUser(user);
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "/users/register";
        }
    }
    /**
     * Muestra la página principal de inicio.
     *
     * @return el nombre de la plantilla correspondiente a la página de inicio ("/users/home").
     */
    @GetMapping(value = {"/home", "/"})
    public String inicioPage() {
        return "/users/home";
    }

    /**
     * Muestra la página de inicio de sesión.
     *
     * @param error  indica si hubo un error durante el intento de inicio de sesión (opcional).
     * @param logout indica si el usuario cerró sesión correctamente (opcional).
     * @param model  el modelo utilizado para pasar atributos a la vista.
     * @return el nombre de la plantilla correspondiente a la página de inicio de sesión ("/users/login").
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        return "/users/login";
    }

    /**
     * Muestra la página "Acerca de nosotros".
     *
     * @return el nombre de la plantilla correspondiente a la página de "Acerca de nosotros" ("/users/aboutUs").
     */
    @GetMapping("/aboutUs")
    public String aboutUsPage() {
        return "/users/aboutUs";
    }

    /**
     * Muestra la página "Contáctanos".
     *
     * @return el nombre de la plantilla correspondiente a la página de "Contáctanos" ("/users/contactUs").
     */
    @GetMapping("/contactUs")
    public String contactUsPage() {
        return "/users/contactUs";
    }

    /**
     * Cierra la sesión del usuario actual y redirige a la página de inicio de sesión.
     *
     * @param request el objeto {@link HttpServletRequest} para obtener la sesión del usuario.
     * @return redirige a la página de inicio de sesión ("/login") después de cerrar sesión.
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}