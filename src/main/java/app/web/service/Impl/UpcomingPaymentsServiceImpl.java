package app.web.service.Impl;

import app.web.service.UpcomingPaymentsService;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.entities.dto.UpcomingPaymentsDTO;
import app.web.persistence.repositories.UpcomingPaymentsRepository;
import app.web.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UpcomingPaymentsServiceImpl implements UpcomingPaymentsService {

    @Autowired
    private UpcomingPaymentsRepository upcomingPaymentsRepository;

    @Autowired
    private UserRepository userRepository;


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

    @Override
    public UpcomingPaymentsEntity createUpcomingPayments(UpcomingPaymentsEntity upcomingPayments, User user) {
        UserEntity userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + user.getUsername()));

        // Crear nueva instancia para evitar problemas con el ID
        UpcomingPaymentsEntity newPayment = new UpcomingPaymentsEntity();
        newPayment.setName(upcomingPayments.getName());
        newPayment.setValue(upcomingPayments.getValue());
        newPayment.setUser(userEntity);

        return upcomingPaymentsRepository.save(newPayment);
    }

    @Override
    public Optional<UpcomingPaymentsEntity> findById(Long id) {
        return upcomingPaymentsRepository.findById(id);
    }
}
