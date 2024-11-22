package app.web.controller;

import app.web.service.UserService;
import app.web.response.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;


    @Operation(summary = "Obtener la sesión actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión obtenida con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/session")
    public ResponseEntity<SessionResponse> getCurrentSession() {
        SessionResponse response = new SessionResponse();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener token CSRF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token CSRF obtenido con éxito")
    })
    @GetMapping("/csrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }


}
