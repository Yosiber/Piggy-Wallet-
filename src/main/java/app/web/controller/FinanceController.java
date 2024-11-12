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
import java.util.*;
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

    @Autowired
    public FinanceController(CashFlowService cashFlowService,
                             CategoryService categoryService,
                             UserService userService) {
        this.cashFlowService = cashFlowService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "/finance/dashboard";
    }

    @GetMapping("/transactions")
    public String mostrarTransacciones(@AuthenticationPrincipal User springUser, Model model) {
        // Obtener el UserEntity para el cashFlowService
        UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

        // Obtener categorías usando el User de Spring Security
        Set<CategoryEntity> allCategories = categoryService.getCategoriesByUser(springUser);

        // Separar categorías en ingresos y gastos
        Map<Boolean, List<CategoryEntity>> categoriesByType = allCategories.stream()
                .collect(Collectors.groupingBy(CategoryEntity::isIncome));

        // Obtener transacciones y balance usando UserEntity
        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);
        Map<String, Object> balance = cashFlowService.getBalanceSummary(currentUser);

        // Agregar atributos al modelo
        model.addAttribute("ingresos", categoriesByType.getOrDefault(true, new ArrayList<>()));
        model.addAttribute("gastos", categoriesByType.getOrDefault(false, new ArrayList<>()));
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
            @AuthenticationPrincipal User springUser) {

        try {
            UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

            // Crear nueva transacción
            CashFlowEntity cashFlow = new CashFlowEntity();
            cashFlow.setUser(currentUser);
            cashFlow.setValue(Float.parseFloat(requestData.get("value").toString()));
            cashFlow.setDescription(requestData.get("description").toString());
            cashFlow.setDate(Timestamp.valueOf(requestData.get("date").toString()));

            // Obtener y validar categoría usando el servicio existente
            Long categoryId = Long.parseLong(requestData.get("categoryId").toString());
            CategoryEntity category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            // Validar que la categoría pertenece al usuario
            if (!category.getUser().getUsername().equals(springUser.getUsername())) {
                throw new RuntimeException("La categoría no pertenece al usuario");
            }

            cashFlow.setCategory(category);

            // Guardar transacción
            CashFlowEntity savedTransaction = cashFlowService.saveTransaction(cashFlow);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
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
            CategoryEntity savedCategory = categoryService.createCategory(category, user);

            return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/analysis")
    public String analysis(@AuthenticationPrincipal User springUser, Model model) {
        UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);

        // Ordenar las transacciones por fecha descendente (más recientes primero)
        transactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        List<CashFlowEntity> ingresos = transactions.stream()
                .filter(transaction -> transaction.getValue() > 0)
                .collect(Collectors.toList());

        List<CashFlowEntity> gastos = transactions.stream()
                .filter(transaction -> transaction.getValue() < 0)
                .collect(Collectors.toList());

        // Si quieres limitar el número de transacciones mostradas
        int limiteMostrar = 10; // Ajusta este número según necesites
        ingresos = ingresos.stream().limit(limiteMostrar).collect(Collectors.toList());
        gastos = gastos.stream().limit(limiteMostrar).collect(Collectors.toList());

        model.addAttribute("transaccionesIngresos", ingresos);
        model.addAttribute("transaccionesGastos", gastos);

        return "finance/analysis";
    }

    @GetMapping("/config")
    public String config() {
        return "/finance/config";
    }
}
