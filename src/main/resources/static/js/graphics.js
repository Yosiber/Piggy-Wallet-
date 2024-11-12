document.addEventListener('DOMContentLoaded', function() {
    // Balance Summary Chart
    const balanceCtx = document.getElementById('balanceChart').getContext('2d');
    const balanceChart = new Chart(balanceCtx, {
        type: 'line',
        data: {
            labels: ['Mar 1', 'Mar 5', 'Mar 10', 'Mar 14'],
            datasets: [{
                label: 'Ingreso',
                data: [1000, 2000, 1500, 2200],
                borderColor: 'rgba(75, 192, 192, 1)',
                fill: false
            }, {
                label: 'Gasto',
                data: [500, 700, 1200, 1000],
                borderColor: 'rgba(255, 99, 132, 1)',
                fill: false
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    // Spending Donut Chart
    const spendingCtx = document.getElementById('spendingDonut').getContext('2d');
    const spendingDonut = new Chart(spendingCtx, {
        type: 'doughnut',
        data: {
            labels: ['Food', 'Shopping', 'Media', 'Transport'],
            datasets: [{
                data: [42, 36, 15, 5],
                backgroundColor: [
                    'rgba(255, 99, 132, 0.7)',
                    'rgba(54, 162, 235, 0.7)',
                    'rgba(255, 206, 86, 0.7)',
                    'rgba(75, 192, 192, 0.7)'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
        }
    });

});


async function addCategory() {
    const categoryName = document.getElementById("categoryName").value;
    const categoryTypeSelect = document.getElementById("categoryType");
    const categoryType = categoryTypeSelect.value === "true";

    // Log para debug
    console.log("Enviando datos:");
    console.log("Nombre:", categoryName);
    console.log("Tipo (value):", categoryTypeSelect.value);
    console.log("Tipo (convertido):", categoryType);

    if (!categoryName.trim()) {
        alert("Por favor ingresa un nombre para la categoría.");
        return;
    }

    const categoryData = {
        name: categoryName,
        income: categoryType
    };

    // Log del objeto final
    console.log("Datos a enviar:", JSON.stringify(categoryData));

    try {
        const response = await fetch("/finance/categories", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(categoryData)
        });

        if (response.ok) {
            const newCategory = await response.json();
            console.log("Categoría creada:", newCategory); // Log de respuesta

            addCategoryToList(newCategory);
            document.getElementById("categoryForm").reset();

            const addCategoryModal = document.getElementById("addCategoryModal");
            const modal = bootstrap.Modal.getInstance(addCategoryModal);
            modal.hide();
        } else {
            const errorData = await response.text();
            console.error("Error response:", errorData);
            alert("Error al agregar la categoría.");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("Error al agregar la categoría.");
    }
}

function addCategoryToList(category) {
    console.log("Agregando categoría a la lista:", category); // Log para debug

    // Obtener la lista correcta basada en el tipo de categoría
    const tabPane = category.income ?
        document.getElementById("ingresos") :
        document.getElementById("gastos");

    const categoriesList = tabPane.querySelector("ul");

    if (!categoriesList) {
        console.error("No se encontró la lista para la categoría:", category);
        return;
    }

    const newCategoryItem = document.createElement("li");
    newCategoryItem.classList.add("list-group-item");
    newCategoryItem.textContent = category.name;
    categoriesList.appendChild(newCategoryItem);
}

// Función para formatear números grandes sin notación científica
function formatearNumero(numero) {
    // Si el número es null o undefined, retornamos 0
    if (numero == null) return '0';

    // Convertimos a número por si acaso viene como string
    const num = typeof numero === 'string' ? parseFloat(numero) : numero;

    // Verificamos si es un número válido
    if (isNaN(num)) return '0';

    // Formateamos el número con separadores de miles y dos decimales
    return num.toLocaleString('es-ES', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
        useGrouping: true // Esto agrega los separadores de miles
    });
}

async function guardarTransaccion(isIncome) {
    const prefix = isIncome ? 'Ingreso' : 'Gasto';
    const formId = isIncome ? 'ingresoForm' : 'gastoForm';
    const form = document.getElementById(formId);

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const monto = parseFloat(document.getElementById(`monto${prefix}`).value);
    const descripcion = document.getElementById(`descripcion${prefix}`).value.trim();
    const categoriaId = parseInt(document.getElementById(`categoria${prefix}`).value, 10);
    const fechaInput = document.getElementById(`fecha${prefix}`).value;

    // Convertir el formato de fecha a 'yyyy-MM-dd HH:mm:ss'
    const fecha = fechaInput.replace("T", " ") + ":00";

    const transactionData = {
        value: isIncome ? Math.abs(monto) : -Math.abs(monto),
        description: descripcion,
        categoryId: categoriaId,
        date: fecha,
    };

    try {
        const response = await fetch("/finance/transactions", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(transactionData)
        });

        if (response.ok) {
            const savedTransaction = await response.json();
            // Cerrar el modal
            const modalId = isIncome ? 'ingresoModal' : 'gastoModal';
            const modal = bootstrap.Modal.getInstance(document.getElementById(modalId));
            modal.hide();

            // Resetear el formulario
            form.reset();

            // Actualizar las tablas y gráficas
            const tableBody = document.getElementById(isIncome ? 'ingresosTableBody' : 'gastosTableBody');
            if (tableBody) {
                // Crear nueva fila
                const row = tableBody.insertRow(0); // Insertar al inicio de la tabla

                // Insertar celdas
                const montoCell = row.insertCell(0);
                const descripcionCell = row.insertCell(1);
                const categoriaCell = row.insertCell(2);
                const fechaCell = row.insertCell(3);

                // Asignar valores
                montoCell.textContent = savedTransaction.value.toLocaleString();
                descripcionCell.textContent = savedTransaction.description;
                categoriaCell.textContent = savedTransaction.category.name; // Asumiendo que la respuesta incluye la categoría
                fechaCell.textContent = formatDateToMonth(savedTransaction.date);

                // Actualizar las gráficas
                formatearTodasLasFechas();
            }
        } else {
            const errorData = await response.text();
            console.error("Error del servidor:", errorData);
            alert("Error al guardar la transacción.");
        }
    } catch (error) {
        console.error("Error al conectar con el servidor:", error);
        alert("Error al guardar la transacción.");
    }
}

// Función para formatear la fecha a solo mes
function formatDateToMonth(dateString) {
    try {
        // Primero verificamos si la fecha ya es un mes
        if (['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio',
            'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'].includes(dateString.toLowerCase())) {
            return dateString;
        }

        // Si no es un mes, intentamos convertir la fecha
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            // Si la fecha no es válida, asumimos que es el nombre del mes
            return dateString;
        }
        const options = { month: 'long' };
        return date.toLocaleDateString('es-ES', options);
    } catch (error) {
        console.error('Error en formatDateToMonth:', error);
        return dateString; // Retornamos el string original si hay error
    }
}

// Función para agrupar transacciones por mes y calcular totales
function agruparPorMes(tableRows) {
    const totalesPorMes = {};

    // Convertir HTMLCollection a Array
    Array.from(tableRows).forEach(row => {
        const mes = row.cells[3].textContent.toLowerCase().trim();
        const monto = parseFloat(row.cells[0].textContent.replace(/[^\d.-]/g, ''));

        if (!isNaN(monto)) {
            if (!totalesPorMes[mes]) {
                totalesPorMes[mes] = 0;
            }
            totalesPorMes[mes] += monto;
        }
    });

    return totalesPorMes;
}

// Función para formatear todas las fechas en las tablas
function formatearTodasLasFechas() {
    const ingresosTable = document.getElementById('ingresosTableBody');
    const gastosTable = document.getElementById('gastosTableBody');

    if (ingresosTable && gastosTable) {
        formatearFechasEnTabla(ingresosTable);
        formatearFechasEnTabla(gastosTable);

        // Procesar datos para las gráficas
        const ingresosPorMes = agruparPorMes(ingresosTable.getElementsByTagName('tr'));
        const gastosPorMes = agruparPorMes(gastosTable.getElementsByTagName('tr'));

        // Crear gráficas con los datos procesados
        crearGraficas(ingresosPorMes, gastosPorMes);
    }
}

function formatearFechasEnTabla(tableBody) {
    if (!tableBody) return;

    Array.from(tableBody.getElementsByTagName('tr')).forEach(row => {
        const dateCell = row.cells[3];
        if (dateCell && dateCell.textContent) {
            dateCell.textContent = formatDateToMonth(dateCell.textContent.trim());
        }
    });
}

function crearGraficas(ingresosPorMes, gastosPorMes) {
    // Definir el orden correcto de los meses
    const ordenMeses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
        'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

    // Obtener todos los meses únicos y ordenarlos
    const meses = [...new Set([...Object.keys(ingresosPorMes), ...Object.keys(gastosPorMes)])];
    meses.sort((a, b) => {
        return ordenMeses.indexOf(a.toLowerCase()) - ordenMeses.indexOf(b.toLowerCase());
    });

    // Gráfico de Ingresos
    const ctxIngresos = document.getElementById('myBarChartIncome').getContext('2d');
    new Chart(ctxIngresos, {
        type: 'bar',
        data: {
            labels: meses,
            datasets: [{
                label: 'Ingresos',
                data: meses.map(mes => ingresosPorMes[mes] || 0),
                backgroundColor: 'rgba(40, 167, 69, 0.5)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value.toLocaleString();
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return '$' + context.raw.toLocaleString();
                        }
                    }
                }
            }
        }
    });

    // Gráfico de Gastos
    const ctxGastos = document.getElementById('myBarChartSpending').getContext('2d');
    new Chart(ctxGastos, {
        type: 'bar',
        data: {
            labels: meses,
            datasets: [{
                label: 'Gastos',
                data: meses.map(mes => Math.abs(gastosPorMes[mes]) || 0),
                backgroundColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value.toLocaleString();
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return '$' + context.raw.toLocaleString();
                        }
                    }
                }
            }
        }
    });
}

// Ejecutar cuando el documento esté listo
document.addEventListener('DOMContentLoaded', formatearTodasLasFechas);








