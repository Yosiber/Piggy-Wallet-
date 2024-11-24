package app.web.listener;

import app.web.persistence.entities.AuditCategoryEntity;
import app.web.persistence.entities.AuditTransactionEntity;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.repositories.AuditCategoryRepository;
import app.web.persistence.repositories.AuditTransactionRepository;
import jakarta.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AuditTransactionListener {

    private final AuditCategoryRepository auditCategoryRepository;
    private final AuditTransactionRepository auditTransactionRepository;


    @PrePersist
    private void prePersist(CashFlowEntity transaction) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        AuditTransactionEntity history = new AuditTransactionEntity();
        history.setAmount(transaction.getValue());
        history.setDate(LocalDateTime.now());
        history.setCategory(transaction.getCategory().getName());
        history.setOperation("INSERT");
        history.setUsername(username);

        this.auditTransactionRepository.save(history);
    }

}
