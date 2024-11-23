package app.web.listener;

import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditPaymentsEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UpcomingPaymentsEntity;
import app.web.persistence.repositories.AuditCategoryRepository;
import app.web.persistence.repositories.AuditPaymentsRepository;
import jakarta.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AuditPaymentsListener {
    private final AuditPaymentsRepository auditPaymentsRepository;

    @PrePersist
    private void prePersist(UpcomingPaymentsEntity payment) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        AuditPaymentsEntity history = new AuditPaymentsEntity();
        history.setName(payment.getName());
        history.setDate(LocalDateTime.now());
        history.setAmount(payment.getValue());
        history.setOperation("INSERT");
        history.setUsername(username);
        this.auditPaymentsRepository.save(history);
    }
}

