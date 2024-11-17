package app.web.controller;


import app.web.Service.CashFlowService;
import app.web.Service.CategoryService;
import app.web.Service.UpcomingPaymentsService;
import app.web.Service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.entities.dto.TransactionDTO;
import app.web.persistence.entities.dto.UpcomingPaymentsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.User;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/finance")
public class FinanceApiController {

    @Autowired
    private CashFlowService cashFlowService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private UpcomingPaymentsService upcomingPaymentsService;

    @GetMapping("/dashboard")
    @Operation(
            summary = "Obtener datos del dashboard",
            description = "Este endpoint retorna los datos del dashboard del usuario autenticado, incluyendo ingresos, gastos y balance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Datos obtenidos con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                {
                    "ingresos": 1500,
                    "gastos": 500,
                    "balance": 1000
                }
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe iniciar sesión antes de acceder a este endpoint.",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Unauthorized\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor. Algo salió mal al procesar la solicitud.",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Internal Server Error\"}"))
            )
    })
    public ResponseEntity<Map<String, Object>> getDashboardData(@AuthenticationPrincipal User springUser) {
        Map<String, Object> response = new HashMap<>();
        response.put("ingresos", 1500);
        response.put("gastos", 500);
        response.put("balance", 1000);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/dashboard")
    @Operation(
            summary = "Crear un pago próximo",
            description = "Este endpoint permite al usuario autenticado crear un pago próximo en su lista. El cliente debe proporcionar un objeto JSON con los detalles del pago."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pago próximo creado con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpcomingPaymentsDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida. Verifique los datos proporcionados.",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid request data\"}"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe iniciar sesión antes de acceder a este endpoint.",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Unauthorized\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Internal Server Error\"}"))
            )
    })
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
    @Operation(
            summary = "Obtener el resumen de transacciones del usuario",
            description = "Este endpoint permite al usuario autenticado obtener un resumen de sus transacciones, "
                    + "categorías agrupadas por tipo (ingresos/gastos), y el balance general. Devuelve las primeras "
                    + "10 transacciones más recientes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Resumen de transacciones obtenido con éxito",
                            content = @Content(
                                    mediaType = "text/html",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Respuesta HTML",
                                                    value = "Plantilla HTML con información de transacciones, categorías y balance"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "No autorizado. El usuario no ha iniciado sesión.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content
                    )
            }
    )
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
    @Operation(
            summary = "Crear una nueva transacción",
            description = "Permite a un usuario autenticado crear una nueva transacción, asociándola con una categoría específica."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transacción creada con éxito",
                    content = @Content(schema = @Schema(implementation = TransactionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la solicitud",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Categoría no encontrada\"}"))
            )
    })
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


}