package app.web.controller;

import app.web.service.CashFlowService;
import app.web.service.CategoryService;
import app.web.service.UserService;
import app.web.persistence.entities.RoleEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CashFlowService cashFlowService;

    @GetMapping("/dashboard")
    public String getAdminDashboard(Model model) {

        // Contadores totales
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("activeUsers", userService.countActiveUsers());
        model.addAttribute("totalCategories", categoryService.countAllCategories());
        model.addAttribute("totalTransactions", cashFlowService.countAllTransactions());

        // Últimos usuarios registrados
        model.addAttribute("recentUsers", userService.getRecentUsers(5));

        // Datos para el gráfico de nuevos usuarios por mes
        Map<String, Long> monthlySignups = userService.getMonthlySignups();
        model.addAttribute("monthlySignupsLabels", new ArrayList<>(monthlySignups.keySet()));
        model.addAttribute("monthlySignups", new ArrayList<>(monthlySignups.values()));

        // Datos para el gráfico de distribución de roles
        Map<String, Long> roleDistribution = userService.getRoleDistribution();
        model.addAttribute("roleLabels", new ArrayList<>(roleDistribution.keySet()));
        model.addAttribute("roleDistribution", new ArrayList<>(roleDistribution.values()));

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String getUsersList(Model model) {
        List<UserEntity> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @DeleteMapping("/users/{id}/delete")
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