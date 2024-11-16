package app.web.controller;

import app.web.Service.CashFlowService;
import app.web.Service.CategoryService;

import app.web.Service.Impl.CashFlowServiceImpl;
import app.web.Service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.entities.dto.TransactionDTO;
//import app.web.persistence.entities.dto.TransactionRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    public String getDashboardData(@AuthenticationPrincipal User springUser, Model model) {
        UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());
        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);
        Set<CategoryEntity> allCategories = categoryService.getCategoriesByUser(springUser);

        // Formateador para valores numéricos
        DecimalFormat df = new DecimalFormat("#,##0.00");
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale("es", "ES")));

        // Separar ingresos y gastos y agrupar por fecha (usando Map<String, Double> para la serialización)
        Map<String, Double> ingresosPorMes = new HashMap<>();
        Map<String, Double> gastosPorMes = new HashMap<>();

        transactions.forEach(transaction -> {
            // Convertir la fecha a formato ISO para que JavaScript pueda procesarla
            String fechaISO = transaction.getDate().toLocalDateTime().toLocalDate().toString();
            double value = transaction.getValue();

            if (value > 0) {
                ingresosPorMes.merge(fechaISO, value, Double::sum);
            } else {
                gastosPorMes.merge(fechaISO, Math.abs(value), Double::sum);
            }
        });

        List<CashFlowEntity> recentTransactions = cashFlowService.getTransactionsByUser(currentUser);

        // Limitar a 10 transacciones
        List<CashFlowEntity> limitedTransactions = recentTransactions.size() > 5
                ? recentTransactions.subList(0, 5)
                : recentTransactions;

        // Obtener gastos por categoría
        Map<String, BigDecimal> gastosPorCategoria = cashFlowService.getExpensesByCategory(currentUser);


        // Convertir BigDecimal a String para evitar notación científica
        Map<String, String> gastosPorCategoriaString = gastosPorCategoria.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toPlainString()
                ));

        model.addAttribute("gastosPorCategoria", gastosPorCategoriaString);

        // Pasar los datos al modelo
        model.addAttribute("ingresosPorMes", ingresosPorMes);
        model.addAttribute("gastosPorMes", gastosPorMes);
        model.addAttribute("transactions", limitedTransactions); // Solo las primeras 10 transacciones


        return "/finance/dashboard";
    }

    @GetMapping("/transactions")
    public String mostrarTransacciones(@AuthenticationPrincipal User springUser, Model model) {
        UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());
        Set<CategoryEntity> allCategories = categoryService.getCategoriesByUser(springUser);

        Map<Boolean, List<CategoryEntity>> categoriesByType = allCategories.stream()
                .collect(Collectors.groupingBy(CategoryEntity::isIncome));


        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);

        // Limitar a 10 transacciones
        List<CashFlowEntity> limitedTransactions = transactions.size() > 10
                ? transactions.subList(0, 10)
                : transactions;

        Map<String, Object> balance = cashFlowService.getBalanceSummary(currentUser);

        // Convertir a String para evitar notación científica
        model.addAttribute("totalIngresos", ((BigDecimal) balance.get("totalIncome")).toPlainString());
        model.addAttribute("totalGastos", ((BigDecimal) balance.get("totalExpenses")).toPlainString());
        model.addAttribute("balance", ((BigDecimal) balance.get("balance")).toPlainString());

        model.addAttribute("ingresos", categoriesByType.getOrDefault(true, new ArrayList<>()));
        model.addAttribute("gastos", categoriesByType.getOrDefault(false, new ArrayList<>()));
        model.addAttribute("transactions", limitedTransactions); // Solo las primeras 10 transacciones

        return "finance/transactions";
    }

    @PostMapping("/transactions")
    @Operation(summary = "Crear una nueva transacción", description = "Permite a un usuario autenticado crear una nueva transacción, asociándola con una categoría específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transacción creada con éxito",
                    content = @Content(schema = @Schema(implementation = CashFlowEntity.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Categoría no encontrada\"}")))
    })
    @ResponseBody
    public ResponseEntity<?> createTransaction(
            @RequestBody Map<String, Object> requestData,
            @AuthenticationPrincipal User springUser) {

        try {
            UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

            // Validar y parsear los datos de la transacción
            if (!requestData.containsKey("value") || !requestData.containsKey("description") || !requestData.containsKey("date") || !requestData.containsKey("categoryId")) {
                throw new IllegalArgumentException("Faltan datos requeridos en la solicitud");
            }

            Float value = Float.parseFloat(requestData.get("value").toString());
            String description = requestData.get("description").toString();
            Timestamp date = Timestamp.valueOf(requestData.get("date").toString());
            Long categoryId = Long.parseLong(requestData.get("categoryId").toString());

            // Buscar y validar la categoría
            CategoryEntity category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            if (!category.getUser().getUsername().equals(springUser.getUsername())) {
                throw new RuntimeException("La categoría no pertenece al usuario");
            }

            // Crear y guardar la transacción
            CashFlowEntity cashFlow = new CashFlowEntity();
            cashFlow.setUser(currentUser);
            cashFlow.setValue(value);
            cashFlow.setDescription(description);
            cashFlow.setDate(date);
            cashFlow.setCategory(category);
            CashFlowEntity savedTransaction = cashFlowService.saveTransaction(cashFlow);

            // Crear el DTO para la respuesta
            TransactionDTO transactionDTO = TransactionDTO.builder()
                    .formattedValue(String.format("%.2f", savedTransaction.getValue()))
                    .value(savedTransaction.getValue())
                    .description(savedTransaction.getDescription())
                    .category(savedTransaction.getCategory())
                    .date(savedTransaction.getDate().toLocalDateTime())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(transactionDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/analysis")
    public String analysis(@AuthenticationPrincipal User springUser, Model model) {
        UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());
        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);

        // Ordenar las transacciones por fecha descendente
        transactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        DecimalFormat df = new DecimalFormat("#,##0.00");
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale("es", "ES")));

        List<TransactionDTO> ingresos = transactions.stream()
                .filter(transaction -> transaction.getValue() > 0)
                .map(transaction -> TransactionDTO.builder()
                        .formattedValue(df.format(transaction.getValue()))
                        .value(transaction.getValue())
                        .description(transaction.getDescription())
                        .category(transaction.getCategory())
                        .date(transaction.getDate().toLocalDateTime())  // Convertir Timestamp a LocalDateTime
                        .build())
                .limit(10)
                .collect(Collectors.toList());

        List<TransactionDTO> gastos = transactions.stream()
                .filter(transaction -> transaction.getValue() < 0)
                .map(transaction -> TransactionDTO.builder()
                        .formattedValue(df.format(Math.abs(transaction.getValue())))
                        .value(transaction.getValue())
                        .description(transaction.getDescription())
                        .category(transaction.getCategory())
                        .date(transaction.getDate().toLocalDateTime())  // Convertir Timestamp a LocalDateTime
                        .build())
                .limit(10)
                .collect(Collectors.toList());

        model.addAttribute("transaccionesIngresos", ingresos);
        model.addAttribute("transaccionesGastos", gastos);

        return "finance/analysis";
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


    @Operation(summary = "Crear una nueva categoría", description = "Permite a un usuario autenticado crear una nueva categoría asociada a su cuenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada con éxito",
                    content = @Content(schema = @Schema(implementation = CategoryEntity.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Descripción del error interno\"}")))
    })
    @PostMapping("/categories")
    @ResponseBody
    public ResponseEntity<CategoryEntity> createCategory(
            @AuthenticationPrincipal User user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(
                            example = "{\"name\": \"Compras\", \"description\": \"Gastos relacionados a compras\"}")))
            @RequestBody CategoryEntity category) {
        try {
            CategoryEntity savedCategory = categoryService.createCategory(category, user);

            return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/config")
    public String config() {
        return "/finance/config";
    }
}
