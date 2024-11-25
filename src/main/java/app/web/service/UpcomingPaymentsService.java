package app.web.service;

import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.dto.UpcomingPaymentsDTO;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;

/**
 * Servicio que gestiona las operaciones relacionadas con los pagos próximos.
 */
public interface UpcomingPaymentsService {

    /**
     * Obtiene los pagos próximos asociados a un usuario.
     *
     * @param user el usuario cuyos pagos próximos se van a recuperar.
     * @return una lista de objetos DTO que representan los pagos próximos.
     */
    List<UpcomingPaymentsDTO> getUpcomingPaymentsByUser(User user);

    /**
     * Crea un nuevo pago próximo asociado a un usuario.
     *
     * @param upcomingPayments la entidad del pago próximo a crear.
     * @param user el usuario al que pertenece el pago próximo.
     * @return el pago próximo creado.
     */
    UpcomingPaymentsEntity createUpcomingPayments(UpcomingPaymentsEntity upcomingPayments, User user);

    /**
     * Busca un pago próximo por su ID.
     *
     * @param id el ID del pago próximo.
     * @return un `Optional` que contiene el pago próximo encontrado, si existe.
     */
    Optional<UpcomingPaymentsEntity> findById(Long id);
}