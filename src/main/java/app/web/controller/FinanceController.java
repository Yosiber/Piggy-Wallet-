package app.web.controller;

import app.web.service.CashFlowService;
import app.web.service.CategoryService;

import app.web.service.UpcomingPaymentsService;
import app.web.service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.entities.dto.TransactionDTO;
//import app.web.persistence.entities.dto.TransactionRequestDTO;
import app.web.persistence.entities.dto.TransactionRequestDTO;
import app.web.persistence.entities.dto.UpcomingPaymentsDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private CashFlowService cashFlowService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private UpcomingPaymentsService upcomingPaymentsService;

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
        // Obtener el usuario actual desde el servicio
        UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

        // Obtener todas las transacciones asociadas al usuario
        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);

        // Obtener todas las categorías asociadas al usuario
        Set<CategoryEntity> allCategories = categoryService.getCategoriesByUser(springUser);

        // Obtener pagos próximos usando el DTO directamente
        List<UpcomingPaymentsDTO> upcomingPayments = upcomingPaymentsService.getUpcomingPaymentsByUser(springUser);

        // Formateador para valores numéricos
        DecimalFormat df = new DecimalFormat("#,##0.00");
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale("es", "ES")));

        // Separar ingresos y gastos y agrupar por fecha
        Map<String, Double> ingresosPorMes = new HashMap<>();
        Map<String, Double> gastosPorMes = new HashMap<>();

        transactions.forEach(transaction -> {
            String fechaISO = transaction.getDate().toLocalDateTime().toLocalDate().toString();
            double value = transaction.getValue();

            if (value > 0) {
                ingresosPorMes.merge(fechaISO, value, Double::sum);
            } else {
                gastosPorMes.merge(fechaISO, Math.abs(value), Double::sum);
            }
        });

        // Obtener las transacciones recientes (limitado a las últimas 5)
        List<CashFlowEntity> recentTransactions = cashFlowService.getTransactionsByUser(currentUser);
        List<CashFlowEntity> limitedTransactions = recentTransactions.size() > 5
                ? recentTransactions.subList(0, 5)
                : recentTransactions;

        // Obtener gastos por categoría
        Map<String, BigDecimal> gastosPorCategoria = cashFlowService.getExpensesByCategory(currentUser);

        // Convertir BigDecimal a String
        Map<String, String> gastosPorCategoriaString = gastosPorCategoria.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toPlainString()
                ));

        // Añadir todos los atributos al modelo
        model.addAttribute("upcomingPayments", upcomingPayments);
        model.addAttribute("gastosPorCategoria", gastosPorCategoriaString);
        model.addAttribute("ingresosPorMes", ingresosPorMes);
        model.addAttribute("gastosPorMes", gastosPorMes);
        model.addAttribute("transactions", limitedTransactions);
        return "/finance/dashboard";
    }

    @PostMapping("/dashboard")
    public ResponseEntity<UpcomingPaymentsDTO> createUpcomingPayments(
            @AuthenticationPrincipal User user,
            @RequestBody UpcomingPaymentsEntity upcomingPayments) {
        try {
            UpcomingPaymentsEntity savedPayment = upcomingPaymentsService.createUpcomingPayments(upcomingPayments, user);

            // Convertir a DTO
            UpcomingPaymentsDTO dto = new UpcomingPaymentsDTO(
                    savedPayment.getId(),
                    savedPayment.getName(),
                    savedPayment.getValue()
            );

            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating upcoming payment: ", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

        // Obtener el balance original
        Map<String, Object> balanceData = cashFlowService.getBalanceSummary(currentUser);
        BigDecimal rawBalanceBigDecimal = balanceData.get("balance") != null
                ? (BigDecimal) balanceData.get("balance")
                : BigDecimal.ZERO;

        // Convertir a double
        double rawBalance = rawBalanceBigDecimal.doubleValue();

        // Formatear el balance original
        NumberFormat currencyFormat = NumberFormat.getInstance();
        String formattedBalance = currencyFormat.format(rawBalance);

        // Calcular saldo total (balance sin negativos)
        double totalBalance = Math.max(rawBalance, 0);
        String formattedTotalBalance = currencyFormat.format(totalBalance);

        // Pasar valores al modelo
        model.addAttribute("balance", formattedBalance); // Balance original (puede ser negativo)
        model.addAttribute("saldoTotal", formattedTotalBalance); // Balance sin negativos
        model.addAttribute("totalIngresos", currencyFormat.format(balanceData.get("totalIncome")));
        model.addAttribute("totalGastos", currencyFormat.format(balanceData.get("totalExpenses")));
        model.addAttribute("ingresos", categoriesByType.getOrDefault(true, new ArrayList<>()));
        model.addAttribute("gastos", categoriesByType.getOrDefault(false, new ArrayList<>()));
        model.addAttribute("transactions", limitedTransactions); // Solo las primeras 10 transacciones

        return "finance/transactions";
    }
    @PostMapping("/transactions")
    @ResponseBody
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO,
            @AuthenticationPrincipal User springUser) {
        try {
            UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

            // Buscar y validar la categoría
            CategoryEntity category = categoryService.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            if (!category.getUser().getUsername().equals(springUser.getUsername())) {
                throw new RuntimeException("La categoría no pertenece al usuario");
            }

            // Crear y guardar la transacción
            CashFlowEntity cashFlow = new CashFlowEntity();
            cashFlow.setUser(currentUser);
            cashFlow.setValue(requestDTO.getValue());
            cashFlow.setDescription(requestDTO.getDescription());
            cashFlow.setDate(Timestamp.valueOf(requestDTO.getDate()));
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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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


    @PostMapping("/categories")
    @ResponseBody
    public ResponseEntity<CategoryEntity> createCategory(
            @AuthenticationPrincipal User user,
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
