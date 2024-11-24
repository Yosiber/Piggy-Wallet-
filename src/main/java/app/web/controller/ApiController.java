package app.web.controller;


import app.web.service.CashFlowService;
import app.web.service.CategoryService;
import app.web.service.UpcomingPaymentsService;
import app.web.service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.entities.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.sql.DataSource;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/finance")
public class ApiController {

    @Autowired
    private CashFlowService cashFlowService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private UpcomingPaymentsService upcomingPaymentsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

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


        List<CashFlowEntity> limitedTransactions = transactions.size() > 10
                ? transactions.subList(0, 10)
                : transactions;

        Map<String, Object> balance = cashFlowService.getBalanceSummary(currentUser);


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


    @GetMapping("/analysis")
    @Operation(
            summary = "Obtener análisis financiero",
            description = "Proporciona un resumen de los ingresos y gastos del usuario autenticado, ordenados por fecha descendente. Devuelve las últimas 10 transacciones de cada tipo (ingresos y gastos)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Análisis financiero obtenido con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FinancialAnalysisDTO.class)
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
    })
    public ResponseEntity<FinancialAnalysisDTO> analysis(@AuthenticationPrincipal User springUser) {
        try {
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
                            .date(transaction.getDate().toLocalDateTime()) // Convertir Timestamp a LocalDateTime
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
                            .date(transaction.getDate().toLocalDateTime()) // Convertir Timestamp a LocalDateTime
                            .build())
                    .limit(10)
                    .collect(Collectors.toList());

            FinancialAnalysisDTO analysis = new FinancialAnalysisDTO(ingresos, gastos);

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @GetMapping("/categories")
    @Operation(
            summary = "Obtener categorías del usuario",
            description = "Devuelve las categorías del usuario autenticado, clasificadas en 'Ingresos' y 'Gastos'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorías obtenidas con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoriesDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado. El usuario no ha iniciado sesión.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor.",
                    content = @Content
            )
    })
    public ResponseEntity<CategoriesDTO> getCategories(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        Set<CategoryEntity> categories = categoryService.getCategoriesByUser(user);

        Set<CategoryDTO> ingresos = categories.stream()
                .filter(CategoryEntity::isIncome)
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toSet());

        Set<CategoryDTO> gastos = categories.stream()
                .filter(category -> !category.isIncome())
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toSet());

        CategoriesDTO response = new CategoriesDTO(ingresos, gastos);

        return ResponseEntity.ok(response);
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

    @Operation(
            summary = "Obtener el perfil del usuario",
            description = """
        Este endpoint permite obtener la información del perfil del usuario autenticado. 
        Incluye datos personales del usuario, estadísticas relacionadas con las transacciones (ingresos, gastos, balance total), 
        y el número de categorías activas asociadas al usuario.
        Devuelve una vista HTML con todos estos detalles pre-renderizados.
    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil cargado exitosamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirección en caso de error (generalmente a una página de error)",
                    content = @Content(mediaType = "text/html")
            )
    })
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

    @Operation(
            summary = "Actualizar el perfil del usuario",
            description = """
        Este endpoint permite actualizar los datos del perfil del usuario autenticado. 
        Se pueden modificar el nombre de usuario, el correo electrónico y el número de teléfono. 
        Si el nombre de usuario o correo ya están en uso, se devolverá un mensaje de error. 
        En caso de éxito, se redirige al perfil o se solicita un nuevo inicio de sesión si se cambió el nombre de usuario.
    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirección en caso de éxito o error",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la solicitud, como username o email ya existente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"errorMessage\": \"Username already exists\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al procesar la solicitud",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"errorMessage\": \"Error updating profile\"}"))
            )
    })
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

    @Operation(summary = "Acceder a la página de configuración", description = "Devuelve la vista de configuración del usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vista cargada correctamente",
                    content = {@Content(mediaType = "text/html")})
    })
    @GetMapping("/config")
    public String config() {
        return "/finance/config";
    }


    @Operation(
            summary = "Redirige al usuario según su rol",
            description = "Requiere que el usuario esté autenticado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirección a la URL correspondiente al rol del usuario."),
            @ApiResponse(responseCode = "401", description = "El usuario no está autenticado o la sesión no es válida."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor al procesar la solicitud.")
    })
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

    @Operation(
            summary = "Maneja errores genéricos de la aplicación",
            description = "Intercepta errores HTTP y redirige según el código de estado, o muestra una página de error genérica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirección al home si el error es un 404 (Not Found)."),
            @ApiResponse(responseCode = "200", description = "Carga de la página genérica de error si no se redirige."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor durante el manejo del error.")
    })
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "redirect:/home";
            }
        }

        return "error";
    }

    @Operation(
            summary = "Obtiene el dashboard administrativo",
            description = "Proporciona estadísticas generales del sistema, como el conteo total de usuarios, categorías, transacciones, "
                    + "así como datos gráficos como nuevos usuarios por mes y distribución de roles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard administrativo cargado con éxito."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor al obtener los datos del dashboard.")
    })
    @GetMapping("/admin/dashboard")
    public String getAdminDashboard(Model model) {

        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("activeUsers", userService.countActiveUsers());
        model.addAttribute("totalCategories", categoryService.countAllCategories());
        model.addAttribute("totalTransactions", cashFlowService.countAllTransactions());

        model.addAttribute("recentUsers", userService.getRecentUsers(5));

        Map<String, Long> monthlySignups = userService.getMonthlySignups();
        model.addAttribute("monthlySignupsLabels", new ArrayList<>(monthlySignups.keySet()));
        model.addAttribute("monthlySignups", new ArrayList<>(monthlySignups.values()));

        Map<String, Long> roleDistribution = userService.getRoleDistribution();
        model.addAttribute("roleLabels", new ArrayList<>(roleDistribution.keySet()));
        model.addAttribute("roleDistribution", new ArrayList<>(roleDistribution.values()));

        return "admin/dashboard";
    }

    @Operation(
            summary = "Eliminar un usuario por ID",
            description = "Permite eliminar un usuario específico por su ID, salvo que sea la cuenta del usuario administrador en sesión.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "El identificador único del usuario que se desea eliminar.",
                            required = true,
                            example = "123"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "El usuario fue eliminado correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HashMap.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud, como intentar eliminar la propia cuenta o un error inesperado.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HashMap.class)))
    })
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

    @Operation(
            summary = "Crear un nuevo usuario",
            description = "Permite crear un nuevo usuario validando su nombre de usuario, contraseña y correo electrónico. "
                    + "Retorna mensajes específicos en caso de error."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario creado correctamente.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HashMap.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud, como datos faltantes o duplicados.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HashMap.class)))
    })
    @PostMapping("/users/create")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody UserEntity newUser) {
        try {
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

            if (userService.existsByUsername(newUser.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "El nombre de usuario ya está en uso.");
                        }});
            }

            if (userService.existsByEmail(newUser.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {{
                            put("error", "El email ya está en uso.");
                        }});
            }

            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

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

    @Operation(
            summary = "Generar reporte de usuarios en formato PDF",
            description = "Genera y envía un archivo PDF con información de los usuarios, basado en una plantilla JasperReports."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte generado y enviado exitosamente en formato PDF."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor durante la generación del reporte.")
    })
    @GetMapping("/generarReporte")
    public void generarReporte(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Reporte_usuarios.pdf");

        JasperReport reporte = JasperCompileManager
                .compileReport(resourceLoader.getResource("classpath:Report.jrxml").getInputStream());

        try (Connection conexion = dataSource.getConnection()) {
            JasperPrint jasperPrint = JasperFillManager.fillReport(reporte, null, conexion);

            try (OutputStream salida = response.getOutputStream()) {
                JasperExportManager.exportReportToPdfStream(jasperPrint, salida);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al generar el reporte: " + e.getMessage());
        }
    }
}
