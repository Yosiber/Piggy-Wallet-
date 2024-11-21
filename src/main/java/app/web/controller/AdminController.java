package app.web.controller;

import app.web.Service.UserService;
import app.web.persistence.entities.RoleEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String getAdminDashboard(Model model) {
        model.addAttribute("pageTitle", "Panel de Administración");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String getUsersList(Model model) {
        List<UserEntity> users = userService.getAllUsers(); // Necesitarás añadir este método al servicio
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", "Gestión de Usuarios");
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            UserEntity currentUser = userService.getCurrentSession();
            if (currentUser.getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "No puedes eliminar tu propia cuenta de administrador");
                        }});
            }
            userService.deleteUser(id);
            return ResponseEntity.ok()
                    .body(new HashMap<String, String>() {{
                        put("message", "Usuario eliminado correctamente");
                    }});
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new HashMap<String, String>() {{
                        put("error", "Error al eliminar el usuario: " + e.getMessage());
                    }});
        }
    }

    @GetMapping("/users/{id}/roles")
    @ResponseBody
    public ResponseEntity<?> getUserRoles(@PathVariable Long id) {
        try {
            UserEntity user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user.getRoles());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener roles: " + e.getMessage());
        }
    }

    @PostMapping("/users/{id}/roles")
    @ResponseBody
    public ResponseEntity<?> updateUserRoles(@PathVariable Long id, @RequestBody Set<RoleEntity> roles) {
        try {
            UserEntity user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            user.setRoles(roles);
            userService.saveUser(user);
            return ResponseEntity.ok()
                    .body(new HashMap<String, String>() {{
                        put("message", "Roles actualizados correctamente");
                    }});
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new HashMap<String, String>() {{
                        put("error", "Error al actualizar roles: " + e.getMessage());
                    }});
        }
    }
}