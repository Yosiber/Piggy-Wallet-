package app.web.persistence.entities.dto;

import lombok.Data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para manejar las actualizaciones de datos de un usuario.
 *
 * <p>Proporciona campos para actualizar el nombre de usuario, el correo electrónico,
 * y el número de teléfono. Esta clase se utiliza para transferir datos entre
 * el cliente y el servidor en operaciones de actualización de usuarios.</p>
 *
 * <p>Incluye constructores predeterminados y con todos los argumentos,
 * facilitando su uso en diferentes contextos.</p>
 *
 * @author TuNombre
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    /**
     * Nuevo nombre de usuario. Puede ser opcional si no se desea modificar.
     */
    private String username;

    /**
     * Nuevo correo electrónico. Puede ser opcional si no se desea modificar.
     */
    private String email;

    /**
     * Nuevo número de teléfono. Puede ser opcional si no se desea modificar.
     */
    private String phone;
}
