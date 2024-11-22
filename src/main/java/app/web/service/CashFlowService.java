package app.web.service;

import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.UserEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CashFlowService {
    CashFlowEntity saveTransaction(CashFlowEntity cashFlow);
    List<CashFlowEntity> getTransactionsByUser(UserEntity user);
    Map<String, Object> getBalanceSummary(UserEntity user);
    Map<String, BigDecimal> getExpensesByCategory(UserEntity user);
    long countAllTransactions();
}