package app.web.Service;

import app.web.persistence.entities.UpcomingPaymentsEntity;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;
import java.util.Set;

public interface UpcomingPaymentsService {
    Set<UpcomingPaymentsEntity> getUpcomingPaymentsByUser(User user);
    UpcomingPaymentsEntity createUpcomingPayments(UpcomingPaymentsEntity upcomingPayments, User user);
    Optional<UpcomingPaymentsEntity> findById(Long id);

}
