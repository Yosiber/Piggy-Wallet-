package app.web.controller;

import app.web.persistence.entities.dto.UserUpdateDTO;
import app.web.service.CashFlowService;
import app.web.service.CategoryService;

import app.web.service.UpcomingPaymentsService;
import app.web.service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.entities.dto.TransactionDTO;
import app.web.persistence.entities.dto.TransactionRequestDTO;
import app.web.persistence.entities.dto.UpcomingPaymentsDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@Transactional
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
        UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

        List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);

        Set<CategoryEntity> allCategories = categoryService.getCategoriesByUser(springUser);

        List<UpcomingPaymentsDTO> upcomingPayments = upcomingPaymentsService.getUpcomingPaymentsByUser(springUser);

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

        List<CashFlowEntity> recentTransactions = cashFlowService.getTransactionsByUser(currentUser);
        List<CashFlowEntity> limitedTransactions = recentTransactions.size() > 5
                ? recentTransactions.subList(0, 5)
                : recentTransactions;

        Map<String, BigDecimal> gastosPorCategoria = cashFlowService.getExpensesByCategory(currentUser);

        Map<String, String> gastosPorCategoriaString = gastosPorCategoria.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toPlainString()
                ));

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

        List<CashFlowEntity> limitedTransactions = transactions.size() > 10
                ? transactions.subList(0, 10)
                : transactions;

        Map<String, Object> balanceData = cashFlowService.getBalanceSummary(currentUser);
        BigDecimal rawBalanceBigDecimal = balanceData.get("balance") != null
                ? (BigDecimal) balanceData.get("balance")
                : BigDecimal.ZERO;

        double rawBalance = rawBalanceBigDecimal.doubleValue();

        NumberFormat currencyFormat = NumberFormat.getInstance();
        String formattedBalance = currencyFormat.format(rawBalance);

        double totalBalance = Math.max(rawBalance, 0);
        String formattedTotalBalance = currencyFormat.format(totalBalance);


        model.addAttribute("balance", formattedBalance);
        model.addAttribute("saldoTotal", formattedTotalBalance);
        model.addAttribute("totalIngresos", currencyFormat.format(balanceData.get("totalIncome")));
        model.addAttribute("totalGastos", currencyFormat.format(balanceData.get("totalExpenses")));
        model.addAttribute("ingresos", categoriesByType.getOrDefault(true, new ArrayList<>()));
        model.addAttribute("gastos", categoriesByType.getOrDefault(false, new ArrayList<>()));
        model.addAttribute("transactions", limitedTransactions);

        return "finance/transactions";
    }

    @PostMapping("/transactions")
    @ResponseBody
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO,
            @AuthenticationPrincipal User springUser) {
        try {
            UserEntity currentUser = userService.getUserByUsername(springUser.getUsername());

            CategoryEntity category = categoryService.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            if (!category.getUser().getUsername().equals(springUser.getUsername())) {
                throw new RuntimeException("La categoría no pertenece al usuario");
            }

            CashFlowEntity cashFlow = new CashFlowEntity();
            cashFlow.setUser(currentUser);
            cashFlow.setValue(requestDTO.getValue());
            cashFlow.setDescription(requestDTO.getDescription());
            cashFlow.setDate(Timestamp.valueOf(requestDTO.getDate()));
            cashFlow.setCategory(category);
            CashFlowEntity savedTransaction = cashFlowService.saveTransaction(cashFlow);


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
                        .date(transaction.getDate().toLocalDateTime())
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
                        .date(transaction.getDate().toLocalDateTime())
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

        Set<CategoryEntity> ingresos = categories.stream()
                .filter(CategoryEntity::isIncome)
                .collect(Collectors.toSet());

        Set<CategoryEntity> gastos = categories.stream()
                .filter(category -> !category.isIncome())
                .collect(Collectors.toSet());

        model.addAttribute("ingresos", ingresos);
        model.addAttribute("gastos", gastos);

        return "finance/categories";
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

    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            UserEntity currentUser = userService.getCurrentSession();
            model.addAttribute("user", currentUser);

            List<CashFlowEntity> transactions = cashFlowService.getTransactionsByUser(currentUser);
            model.addAttribute("totalTransactions", transactions.size());

            Map<String, Object> balanceSummary = cashFlowService.getBalanceSummary(currentUser);
            model.addAttribute("totalIncome", balanceSummary.get("totalIncome"));
            model.addAttribute("totalExpenses", balanceSummary.get("totalExpenses"));
            model.addAttribute("balance", balanceSummary.get("balance"));

            UserDetails userDetails = User.withUsername(currentUser.getUsername())
                    .password(currentUser.getPassword())
                    .authorities(currentUser.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getRolName()))
                            .collect(Collectors.toList()))
                    .build();

            User springUser = (User) userDetails;


            Set<CategoryEntity> categories = categoryService.getCategoriesByUser(springUser);
            model.addAttribute("activeCategories", categories.size());

            return "finance/profile";
        } catch (Exception e) {
            log.error("Error al cargar el perfil: ", e);
            return "redirect:/error";
        }
    }

    @PutMapping("/profile/update")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        try {
            UserEntity currentUser = userService.getCurrentSession();
            boolean needsRelogin = false;

            if (!username.equals(currentUser.getUsername())) {
                if (userService.existsByUsername(username)) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Username already exists");
                    return "redirect:/finance/profile";
                }
                needsRelogin = true;
            }


            if (!email.equals(currentUser.getEmail()) &&
                    userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Email already exists");
                return "redirect:/finance/profile";
            }


            currentUser.setEmail(email);
            currentUser.setUsername(username);
            if (phone != null && !phone.trim().isEmpty()) {
                currentUser.setPhone(phone);
            }

            userService.saveUser(currentUser);

            if (needsRelogin) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Profile updated successfully. Please login again with your new username");
                return "redirect:/logout";
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Profile updated successfully");
                return "redirect:/finance/profile";
            }

        } catch (Exception e) {
            log.error("Error al actualizar el perfil: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating profile");
            return "redirect:/finance/profile";
        }
    }


    @GetMapping("/config")
    public String config() {
        return "/finance/config";
    }
}
