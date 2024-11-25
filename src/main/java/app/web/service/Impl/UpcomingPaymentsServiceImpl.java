package app.web.service.Impl;

import app.web.service.UpcomingPaymentsService;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.entities.dto.UpcomingPaymentsDTO;
import app.web.persistence.repositories.UpcomingPaymentsRepository;
import app.web.persistence.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de pagos futuros.
 * Proporciona métodos para crear, recuperar y buscar pagos futuros asociados a un usuario.
 */
@Service
@Transactional
public class UpcomingPaymentsServiceImpl implements UpcomingPaymentsService {

    @Autowired
    private UpcomingPaymentsRepository upcomingPaymentsRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene una lista de pagos futuros asociados a un usuario específico.
     *
     * @param user el usuario cuyos pagos futuros se desean obtener.
     * @return una lista de {@link UpcomingPaymentsDTO} que representan los pagos futuros del usuario.
     */
    @Override
    public List<UpcomingPaymentsDTO> getUpcomingPaymentsByUser(User user) {
        Set<UpcomingPaymentsEntity> payments = upcomingPaymentsRepository.findByUserUsername(user.getUsername());
        return payments.stream()
                .map(payment -> new UpcomingPaymentsDTO(
                        payment.getId(),
                        payment.getName(),
                        payment.getValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo pago futuro asociado a un usuario específico.
     *
     * @param upcomingPayments los detalles del pago futuro a crear.
     * @param user el usuario al que se asociará el pago futuro.
     * @return el {@link UpcomingPaymentsEntity} creado y almacenado en la base de datos.
     * @throws RuntimeException si el usuario no se encuentra en la base de datos.
     */
    @Override
    public UpcomingPaymentsEntity createUpcomingPayments(UpcomingPaymentsEntity upcomingPayments, User user) {
        UserEntity userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + user.getUsername()));

        UpcomingPaymentsEntity newPayment = new UpcomingPaymentsEntity();
        newPayment.setName(upcomingPayments.getName());
        newPayment.setValue(upcomingPayments.getValue());
        newPayment.setUser(userEntity);

        return upcomingPaymentsRepository.save(newPayment);
    }

    /**
     * Busca un pago futuro por su ID.
     *
     * @param id el ID del pago futuro a buscar.
     * @return un {@link Optional} que contiene el pago futuro si se encuentra, o vacío si no existe.
     */
    @Override
    public Optional<UpcomingPaymentsEntity> findById(Long id) {
        return upcomingPaymentsRepository.findById(id);
    }
}
