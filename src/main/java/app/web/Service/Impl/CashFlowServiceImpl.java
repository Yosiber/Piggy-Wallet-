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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CashFlowServiceImpl implements CashFlowService {

    private final CashFlowRepository cashFlowRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Autowired
    public CashFlowServiceImpl(CashFlowRepository cashFlowRepository,
                               CategoryRepository categoryRepository,
                               UserService userService) {
        this.cashFlowRepository = cashFlowRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    @Override
    public CashFlowEntity saveTransaction(CashFlowEntity cashFlow) {
        // Validar y obtener la categoría
        CategoryEntity category = validateCategory(cashFlow);

        // Ajustar el valor según el tipo de transacción (ingreso o gasto)
        if (category.isIncome()) {
            cashFlow.setValue(Math.abs(cashFlow.getValue().floatValue()));
        } else {
            cashFlow.setValue(-Math.abs(cashFlow.getValue().floatValue()));
        }

        return cashFlowRepository.save(cashFlow);
    }

    private CategoryEntity validateCategory(CashFlowEntity cashFlow) {
        CategoryEntity category = categoryRepository.findById(cashFlow.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Validar que la categoría pertenezca al usuario
        if (!category.getUser().getId().equals(cashFlow.getUser().getId())) {
            throw new RuntimeException("La categoría no pertenece al usuario");
        }

        return category;
    }

    private double adjustTransactionValue(boolean isIncome, double value) {
        return isIncome ? Math.abs(value) : -Math.abs(value);
    }

    @Override
    public List<CashFlowEntity> getTransactionsByUser(UserEntity user) {
        return cashFlowRepository.findByUserOrderByDateDesc(user);
    }

    @Override
    public Map<String, Object> getBalanceSummary(UserEntity user) {
        List<CashFlowEntity> transactions = getTransactionsByUser(user);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getCategory().isIncome())
                .map(t -> BigDecimal.valueOf(t.getValue())) // Convertir el valor de Float a BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> !t.getCategory().isIncome())
                .map(t -> BigDecimal.valueOf(Math.abs(t.getValue()))) // Convertir el valor a BigDecimal y tomar valor absoluto
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("balance", balance);

        return summary;
    }

    @Override
    public Map<String, BigDecimal> getExpensesByCategory(UserEntity user) {
        List<CashFlowEntity> transactions = getTransactionsByUser(user);

        return transactions.stream()
                .filter(t -> !t.getCategory().isIncome()) // Solo gastos
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.mapping(
                                t -> BigDecimal.valueOf(Math.abs(t.getValue())),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }
}

