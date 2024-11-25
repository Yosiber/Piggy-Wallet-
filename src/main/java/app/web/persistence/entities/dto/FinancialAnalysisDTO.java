package app.web.persistence.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO para representar un análisis financiero, que contiene listas de ingresos y gastos.
 *
 * <p>Se utiliza para transferir datos relacionados con el análisis financiero,
 * categorizados en transacciones de ingresos y gastos.</p>
 *
 * @author TuNombre
 */
@Data
@AllArgsConstructor
public class FinancialAnalysisDTO {

    /**
     * Lista de transacciones de ingresos.
     */
    private List<TransactionDTO> ingresos;

    /**
     * Lista de transacciones de gastos.
     */
    private List<TransactionDTO> gastos;
}
