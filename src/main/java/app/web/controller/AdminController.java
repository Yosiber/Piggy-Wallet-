package app.web.controller;

import app.web.persistence.repositories.RoleRepository;
import app.web.service.CashFlowService;
import app.web.service.CategoryService;
import app.web.service.UserService;
import app.web.persistence.entities.RoleEntity;
import app.web.persistence.entities.UserEntity;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.sql.Connection;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

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

    @PostMapping("/users/create")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody UserEntity newUser) {
        try {
            // Validaciones básicas
            if (newUser.getUsername() == null || newUser.getUsername().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "El nombre de usuario es obligatorio.");
                        }});
            }
            if (newUser.getPassword() == null || newUser.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "La contraseña es obligatoria.");
                        }});
            }

            // Validar si el usuario ya existe
            if (userService.existsByUsername(newUser.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "El nombre de usuario ya está en uso.");
                        }});
            }

            // Validar si el email ya existe
            if (userService.existsByEmail(newUser.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "El email ya está en uso.");
                        }});
            }

            // Encriptar la contraseña antes de crear el usuario
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

            // Crear el usuario
            userService.createUser(newUser);

            return ResponseEntity.ok()
                    .body(new HashMap<String, String>() {{
                        put("message", "Usuario creado correctamente.");
                    }});
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new HashMap<String, String>() {{
                        put("error", "Error al crear el usuario: " + e.getMessage());
                    }});
        }
    }

    @GetMapping("/generarReporte")
    public void generarReporte(HttpServletResponse response) throws Exception {
        // Configurar el tipo de contenido y el encabezado de la respuesta
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_usuarios.pdf");

        // Compilar el reporte
        JasperReport reporte = JasperCompileManager
                .compileReport(resourceLoader.getResource("classpath:Report.jrxml").getInputStream());

        // Usar la conexión de base de datos
        try (Connection conexion = dataSource.getConnection()) {
            // Generar el reporte sin parámetros
            JasperPrint jasperPrint = JasperFillManager.fillReport(reporte, null, conexion);

            // Exportar el reporte como PDF y enviarlo en la respuesta
            try (OutputStream salida = response.getOutputStream()) {
                JasperExportManager.exportReportToPdfStream(jasperPrint, salida);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al generar el reporte: " + e.getMessage());
        }
    }

}