package app.web.service.Impl;

import app.web.service.CashFlowService;
import app.web.service.UserService;
import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import app.web.persistence.repositories.CashFlowRepository;
import app.web.persistence.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de transacciones de flujo de efectivo (CashFlow).
 * Proporciona funcionalidades para guardar transacciones, obtener información sobre el balance,
 * gastos por categoría y contar las transacciones.
 */
@Service
@Transactional
@Slf4j
public class CashFlowServiceImpl implements CashFlowService {

    private final CashFlowRepository cashFlowRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    /**
     * Constructor que inyecta las dependencias requeridas.
     *
     * @param cashFlowRepository repositorio de flujo de efectivo.
     * @param categoryRepository repositorio de categorías.
     * @param userService servicio de usuarios.
     */
    @Autowired
    public CashFlowServiceImpl(CashFlowRepository cashFlowRepository,
                               CategoryRepository categoryRepository,
                               UserService userService) {
        this.cashFlowRepository = cashFlowRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    /**
     * Guarda una transacción de flujo de efectivo después de validar la categoría
     * y ajustar el valor según sea ingreso o gasto.
     *
     * @param cashFlow la transacción a guardar.
     * @return la transacción guardada.
     */
    @Override
    public CashFlowEntity saveTransaction(CashFlowEntity cashFlow) {
        CategoryEntity category = validateCategory(cashFlow);

        if (category.isIncome()) {
            cashFlow.setValue(Math.abs(cashFlow.getValue().floatValue()));
        } else {
            cashFlow.setValue(-Math.abs(cashFlow.getValue().floatValue()));
        }

        return cashFlowRepository.save(cashFlow);
    }

    /**
     * Valida si la categoría asociada a una transacción existe y pertenece al usuario correcto.
     *
     * @param cashFlow la transacción que contiene la categoría a validar.
     * @return la categoría validada.
     * @throws RuntimeException si la categoría no existe o no pertenece al usuario.
     */
    private CategoryEntity validateCategory(CashFlowEntity cashFlow) {
        CategoryEntity category = categoryRepository.findById(cashFlow.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (!category.getUser().getId().equals(cashFlow.getUser().getId())) {
            throw new RuntimeException("La categoría no pertenece al usuario");
        }

        return category;
    }

    /**
     * Obtiene todas las transacciones de flujo de efectivo de un usuario, ordenadas por fecha descendente.
     *
     * @param user el usuario cuyas transacciones se quieren obtener.
     * @return una lista de transacciones del usuario.
     */
    @Override
    public List<CashFlowEntity> getTransactionsByUser(UserEntity user) {
        return cashFlowRepository.findByUserOrderByDateDesc(user);
    }

    /**
     * Calcula un resumen del balance para un usuario, incluyendo ingresos totales,
     * gastos totales y el balance neto.
     *
     * @param user el usuario para el cual se genera el resumen.
     * @return un mapa con las claves "totalIncome", "totalExpenses" y "balance".
     */
    @Override
    public Map<String, Object> getBalanceSummary(UserEntity user) {
        List<CashFlowEntity> transactions = getTransactionsByUser(user);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getCategory().isIncome())
                .map(t -> BigDecimal.valueOf(t.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> !t.getCategory().isIncome())
                .map(t -> BigDecimal.valueOf(Math.abs(t.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("balance", balance);

        return summary;
    }

    /**
     * Calcula los gastos de un usuario agrupados por categoría.
     *
     * @param user el usuario cuyas transacciones se agrupan.
     * @return un mapa donde las claves son los nombres de las categorías y los valores son los gastos totales.
     */
    @Override
    public Map<String, BigDecimal> getExpensesByCategory(UserEntity user) {
        List<CashFlowEntity> transactions = getTransactionsByUser(user);

        return transactions.stream()
                .filter(t -> !t.getCategory().isIncome())
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.mapping(
                                t -> BigDecimal.valueOf(Math.abs(t.getValue())),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    /**
     * Cuenta el número total de transacciones de flujo de efectivo en el sistema.
     *
     * @return el número total de transacciones.
     */
    @Override
    public long countAllTransactions() {
        return cashFlowRepository.count();
    }
}

