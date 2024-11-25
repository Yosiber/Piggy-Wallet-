package app.web.service;

import app.web.persistence.entities.CashFlowEntity;
import app.web.persistence.entities.UserEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Servicio que gestiona las operaciones relacionadas con los flujos de efectivo (Cash Flow).
 */
public interface CashFlowService {

    /**
     * Guarda una transacción de flujo de efectivo en la base de datos.
     *
     * @param cashFlow la entidad de flujo de efectivo a guardar.
     * @return la entidad de flujo de efectivo guardada.
     */
    CashFlowEntity saveTransaction(CashFlowEntity cashFlow);

    /**
     * Obtiene todas las transacciones de flujo de efectivo asociadas a un usuario.
     *
     * @param user el usuario cuyos flujos de efectivo se van a recuperar.
     * @return una lista de transacciones ordenadas por fecha descendente.
     */
    List<CashFlowEntity> getTransactionsByUser(UserEntity user);

    /**
     * Calcula el balance general de ingresos y gastos de un usuario.
     *
     * @param user el usuario para el cual se genera el resumen.
     * @return un mapa que contiene el total de ingresos, gastos y el balance actual.
     */
    Map<String, Object> getBalanceSummary(UserEntity user);

    /**
     * Agrupa los gastos de un usuario por categoría y calcula el monto total para cada una.
     *
     * @param user el usuario cuyos gastos se van a analizar.
     * @return un mapa donde las claves son nombres de categorías y los valores son los montos totales.
     */
    Map<String, BigDecimal> getExpensesByCategory(UserEntity user);

    /**
     * Cuenta el número total de transacciones registradas.
     *
     * @return el número total de transacciones.
     */
    long countAllTransactions();
}