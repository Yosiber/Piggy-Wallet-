package app.web.Service.Impl;

import app.web.Service.UpcomingPaymentsService;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.repositories.UpcomingPaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UpcomingPaymentsServiceImpl implements UpcomingPaymentsService {

    @Autowired
    private UpcomingPaymentsRepository upcomingPaymentsRepository;

    @Override
    public Set<UpcomingPaymentsEntity> getUpcomingPaymentsByUser(User user) {
        return Set.of();
    }

    @Override
    public UpcomingPaymentsEntity createUpcomingPayments(UpcomingPaymentsEntity upcomingPayments, User user) {
        return null;
    }

    @Override
    public Optional<UpcomingPaymentsEntity> findById(Long id) {
        return upcomingPaymentsRepository.findById(id);
    }
}
