package app.web.Service.Impl;

import app.web.Service.CashFlowService;
import app.web.Service.CategoryService;
import app.web.Service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.repositories.CashFlowRepository;
import app.web.persistence.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@Slf4j
public class CashFlowServiceImpl implements CashFlowService {

    @Autowired
    private CashFlowRepository cashFlowRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @Override
    public CashFlowEntity saveTransaction(CashFlowEntity cashFlow) {
        UserEntity currentUser = userService.getCurrentSession();
        cashFlow.setUser(currentUser);
        cashFlow.setDate(new Timestamp(System.currentTimeMillis()));
        return cashFlowRepository.save(cashFlow);
    }

    @Override
    public List<CashFlowEntity> getTransactionsByUser(UserEntity user) {
        return cashFlowRepository.findByUser(user);
    }

    @Override
    public List<CashFlowEntity> getTransactionsByPeriod(LocalDateTime startDate, LocalDateTime endDate, UserEntity user) {
        return cashFlowRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    @Override
    public Map<String, Double> getBalanceSummary(UserEntity user) {
        List<CashFlowEntity> transactions = cashFlowRepository.findByUser(user);

        double totalIncome = transactions.stream()
                .filter(t -> t.getCategory().isIncome())
                .mapToDouble(CashFlowEntity::getValue)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(t -> !t.getCategory().isIncome())
                .mapToDouble(CashFlowEntity::getValue)
                .sum();

        Map<String, Double> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("balance", totalIncome - totalExpenses);

        log.info("Balance calculado para usuario {}: {}", user.getUsername(), summary);

        return summary;
    }
}

