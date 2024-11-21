package app.web.controller;

import app.web.Service.UserService;
import app.web.persistence.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessController {

    private final UserService userService;

    @Autowired
    public AccessController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/access")
    public String access() {
        UserEntity user = userService.getCurrentSession();

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRolName().equals("ADMIN"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/finance/dashboard";
    }
}
