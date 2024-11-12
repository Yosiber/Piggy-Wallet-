package app.web.Service;

import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CashFlowService {
    CashFlowEntity saveTransaction(CashFlowEntity cashFlow);
    List<CashFlowEntity> getTransactionsByUser(UserEntity user);
    Map<String, Object> getBalanceSummary(UserEntity user);
}