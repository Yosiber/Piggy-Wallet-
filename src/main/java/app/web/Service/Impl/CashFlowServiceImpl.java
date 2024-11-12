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

    @Autowired
    public CashFlowServiceImpl(CashFlowRepository cashFlowRepository,
                               CategoryRepository categoryRepository) {
        this.cashFlowRepository = cashFlowRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CashFlowEntity saveTransaction(CashFlowEntity cashFlow) {
        // Obtener la categoría y validar
        CategoryEntity category = categoryRepository.findById(cashFlow.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Validar que la categoría pertenezca al usuario
        if (!category.getUser().getId().equals(cashFlow.getUser().getId())) {
            throw new RuntimeException("La categoría no pertenece al usuario");
        }

        // Ajustar el valor según el tipo de transacción (ingreso o gasto)
        if (category.isIncome()) {
            cashFlow.setValue(Math.abs(cashFlow.getValue()));
        } else {
            cashFlow.setValue(-Math.abs(cashFlow.getValue()));
        }

        return cashFlowRepository.save(cashFlow);
    }

    @Override
    public List<CashFlowEntity> getTransactionsByUser(UserEntity user) {
        return cashFlowRepository.findByUserOrderByDateDesc(user);
    }

    @Override
    public Map<String, Object> getBalanceSummary(UserEntity user) {
        List<CashFlowEntity> transactions = getTransactionsByUser(user);

        double totalIncome = transactions.stream()
                .filter(t -> t.getCategory().isIncome())
                .mapToDouble(CashFlowEntity::getValue)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(t -> !t.getCategory().isIncome())
                .mapToDouble(t -> Math.abs(t.getValue()))
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("balance", totalIncome - totalExpenses);

        return summary;
    }
}

