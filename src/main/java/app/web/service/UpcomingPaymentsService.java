package app.web.service;

import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.entities.dto.UpcomingPaymentsDTO;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;

public interface UpcomingPaymentsService {
    List<UpcomingPaymentsDTO> getUpcomingPaymentsByUser(User user);
    UpcomingPaymentsEntity createUpcomingPayments(UpcomingPaymentsEntity upcomingPayments, User user);
    Optional<UpcomingPaymentsEntity> findById(Long id);

}
