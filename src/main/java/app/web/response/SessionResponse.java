package app.web.response;

import app.web.persistence.entities.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase que representa la respuesta de una sesión de usuario.
 * Contiene información sobre el usuario, hora de inicio de sesión, ID de sesión, roles y estado de la sesión.
 */
@Data
public class SessionResponse {

    /** Información del usuario que inició sesión. */
    private UserEntity user;

    /** Hora en que se inició la sesión. */
    private LocalDateTime loginTime;

    /** ID único asignado a la sesión. */
    private String sessionId;

    /** Lista de roles o autoridades asociadas al usuario. */
    private List<String> authorities;

    /** Estado de la sesión (activa o no). */
    private boolean active;
}