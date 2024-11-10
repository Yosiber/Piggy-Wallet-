package app.web.controller;

import app.web.Service.CashFlowService;
import app.web.Service.CategoryService;

import app.web.Service.Impl.CashFlowServiceImpl;
import app.web.Service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private CashFlowService cashFlowService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    public FinanceController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping("/dashboard")
    public String dashboard() {
        return "/finance/dashboard";
    }

    @GetMapping("/transactions")
    public String mostrarTransacciones(@AuthenticationPrincipal User user, Model model) {
        // Obtener usuario
        UserEntity currentUser = userService.getUserByUsername(user.getUsername());

        // Obtener categorías del usuario
        Set<CategoryEntity> categories = categoryService.getCategoriesByUser(user);

        // Separar categorías en ingresos y gastos
        Set<CategoryEntity> ingresos = categories.stream()
                .filter(CategoryEntity::isIncome)
                .collect(Collectors.toSet());

        Set<CategoryEntity> gastos = categories.stream()
                .filter(category -> !category.isIncome())
                .collect(Collectors.toSet());

        // Obtener transacciones y balance
        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);
        Map<String, Double> balance = cashFlowService.getBalanceSummary(currentUser);

        // Agregar atributos al modelo
        model.addAttribute("ingresos", ingresos);
        model.addAttribute("gastos", gastos);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalIngresos", balance.get("totalIncome"));
        model.addAttribute("totalGastos", balance.get("totalExpenses"));
        model.addAttribute("balance", balance.get("balance"));

        return "finance/transactions";
    }

    @PostMapping("/transactions")
    @ResponseBody
    public ResponseEntity<?> createTransaction(
            @RequestBody Map<String, Object> requestData,
            @AuthenticationPrincipal User user
    ) {
        try {
            UserEntity currentUser = userService.getUserByUsername(user.getUsername());

            CashFlowEntity cashFlow = new CashFlowEntity();
            cashFlow.setUser(currentUser);

            // Validar y asignar los campos básicos
            if (requestData.containsKey("value")) {
                cashFlow.setValue(Float.parseFloat(requestData.get("value").toString()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Field 'value' is missing"));
            }

            if (requestData.containsKey("description")) {
                cashFlow.setDescription(requestData.get("description").toString());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Field 'description' is missing"));
            }

            if (requestData.containsKey("date")) {
                cashFlow.setDate(Timestamp.valueOf(requestData.get("date").toString()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Field 'date' is missing"));
            }

            // Validar y asignar la categoría basada en categoryId
            if (requestData.containsKey("categoryId")) {
                Long categoryId = Long.parseLong(requestData.get("categoryId").toString());
                Optional<CategoryEntity> categoryOpt = categoryService.findById(categoryId);

                if (categoryOpt.isPresent()) {
                    cashFlow.setCategory(categoryOpt.get());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Category with ID " + categoryId + " not found"));
                }
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Field 'categoryId' is missing"));
            }

            // Guardar la transacción
            CashFlowEntity savedTransaction = cashFlowService.saveTransaction(cashFlow);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid format for number field(s): " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid data: " + e.getMessage()));
        } catch (RuntimeException e) {
            System.out.println("Error al crear la transacción: " + e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<List<CashFlowEntity>> getTransactionsByPeriod(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @AuthenticationPrincipal User user
    ) {
        UserEntity currentUser = userService.getUserByUsername(user.getUsername());
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByPeriod(start, end, currentUser);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/balance")
    @ResponseBody
    public ResponseEntity<Map<String, Double>> getBalance(@AuthenticationPrincipal User user) {
        UserEntity currentUser = userService.getUserByUsername(user.getUsername());
        Map<String, Double> balance = cashFlowService.getBalanceSummary(currentUser);
        return ResponseEntity.ok(balance);
    }


    // Manejador de excepciones específico para este controlador
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        System.out.println("Error en el controlador de transacciones" + e);
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }


    @GetMapping("/categories")
    public String mostrarCategorias(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user, Model model) {
        Set<CategoryEntity> categories = categoryService.getCategoriesByUser(user);

        // Filtra las categorías en "Ingresos" y "Gastos"
        Set<CategoryEntity> ingresos = categories.stream()
                .filter(CategoryEntity::isIncome)
                .collect(Collectors.toSet()); // Cambia a Collectors.toSet()

        Set<CategoryEntity> gastos = categories.stream()
                .filter(category -> !category.isIncome())
                .collect(Collectors.toSet()); // Cambia a Collectors.toSet()

        model.addAttribute("ingresos", ingresos);
        model.addAttribute("gastos", gastos);

        return "finance/categories";  // Asegúrate de que la vista esté en la ruta correcta
    }

    @PostMapping("/categories")
    @ResponseBody
    public ResponseEntity<CategoryEntity> createCategory(@AuthenticationPrincipal User user, @RequestBody CategoryEntity category) {
        try {
            // Logs para debug
            System.out.println("Datos recibidos:");
            System.out.println("Nombre categoría: " + category.getName());
            System.out.println("isIncome (antes): " + category.isIncome());

            CategoryEntity savedCategory = categoryService.createCategory(category, user);

            System.out.println("Categoría guardada:");
            System.out.println("ID: " + savedCategory.getId());
            System.out.println("Nombre: " + savedCategory.getName());
            System.out.println("isIncome (después): " + savedCategory.isIncome());

            return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/analysis")
    public String analysis() {
        return "/finance/analysis";
    }

    @GetMapping("/config")
    public String config() {
        return "/finance/config";
    }
}
