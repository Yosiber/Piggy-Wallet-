package app.web.listener;

import app.web.persistence.entities.AuditUserEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.repositories.AuditUserRepository;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AuditUserListener {

    private final AuditUserRepository auditUserRepository;

    @PreRemove
    private void preRemove(UserEntity user) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "system";

            AuditUserEntity history = new AuditUserEntity();
            history.setName(user.getUsername());
            history.setDate(LocalDateTime.now());
            history.setOperation("DELETE");
            history.setUsername(username);
            this.auditUserRepository.save(history);
    }

    @PreUpdate
    private void preUpdate(UserEntity user) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "system";

            AuditUserEntity history = new AuditUserEntity();
            history.setName(user.getUsername());
            history.setDate(LocalDateTime.now());
            history.setOperation("UPDATE");
            history.setUsername(username);
            this.auditUserRepository.save(history);
    }


    @PrePersist
    private void prePersist(UserEntity user) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        AuditUserEntity history = new AuditUserEntity();
        history.setName(user.getUsername());
        history.setDate(LocalDateTime.now());
        history.setOperation("INSERT");
        history.setUsername(username);
        this.auditUserRepository.save(history);
    }
}