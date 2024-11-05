package app.web.controller;

import app.web.Service.UserService;
import app.web.persistence.entities.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @GetMapping("/record")
    public String recordPage() {
        return "/users/register";
    }

    @PostMapping("/register")
    public String registe(UserEntity user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userService.createUser(user);
        return "redirect:/login";
    }

    @GetMapping(value = {"/home", "/"})
    public String inicioPage(){
        return "/users/home";
    }

    @GetMapping("/login")
    public String loginPage(){
        return "/users/login";
    }

    @GetMapping("/aboutUs")
    public String aboutUsPage(){
        return "/users/aboutUs";
    }

    @GetMapping("/contactUs")
    public String contactUsPage(){
        return "/users/contactUs";
    }


    @GetMapping("/access")
    public String access(HttpSession session) {
        Optional<UserEntity> optionalUser = userService.getUserById(Long.parseLong(session.getAttribute("user_session_id").toString()));

        if (optionalUser.isPresent()) {
            session.setAttribute("user_session_id", optionalUser.get().getId());
            return "redirect:/finance/dashboard";
        } else {
            return "redirect:/login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}