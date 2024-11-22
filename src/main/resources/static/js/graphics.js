// Obtener el token CSRF y el nombre del header de los meta tags
const token = document.querySelector("meta[name='_csrf']").getAttribute('content');
const headerName = document.querySelector("meta[name='_csrf_header']").getAttribute('content');
console.log('Token disponible:', !!token);
console.log('Nombre del header disponible:', !!headerName);




    function crearGraficasDashboard(ingresosPorMes, gastosPorMes) {
        const ordenMeses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
            'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

        const meses = [...new Set([...Object.keys(ingresosPorMes), ...Object.keys(gastosPorMes)])];
        meses.sort((a, b) => {
            return ordenMeses.indexOf(a.toLowerCase()) - ordenMeses.indexOf(b.toLowerCase());
        });

        // Función auxiliar para formatear números en las gráficas
        const formatearNumeroGrafica = (valor) => {
            return new Intl.NumberFormat('es-ES', {
                style: 'decimal',
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }).format(valor);
        };

        // Gráfico de Ingresos y Gastos
        const ctxBalanceChart = document.getElementById('balanceChart').getContext('2d');
        new Chart(ctxBalanceChart, {
            type: 'bar',
            data: {
                labels: meses,
                datasets: [
                    {
                        label: 'Ingresos',
                        data: meses.map(mes => ingresosPorMes[mes] || 0),
                        backgroundColor: 'rgba(40, 167, 69, 0.5)',
                        borderWidth: 1
                    },
                    {
                        label: 'Gastos',
                        data: meses.map(mes => Math.abs(gastosPorMes[mes]) || 0),
                        backgroundColor: 'rgba(255, 99, 132, 1)',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return '$' + formatearNumeroGrafica(value);
                            }
                        }
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return '$' + formatearNumeroGrafica(context.raw);
                            }
                        }
                    }
                }
            }
        });
    }






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
        // Crear el objeto de headers
        const headers = {
            "Content-Type": "application/json"
        };
        // Agregar el header CSRF dinámicamente
        headers[headerName] = token;

        const response = await fetch("/finance/categories", {
            method: "POST",
            headers: headers,
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



// Función para formatear números sin notación científica
function formatNumber(number) {
    // Si el número es null o undefined, retornamos 0
    if (number == null) return '0,00';

    // Convertimos a número si es string
    const num = typeof number === 'string' ? parseFloat(number) : number;

    // Verificamos si es un número válido
    if (isNaN(num)) return '0,00';

    try {
        return num.toLocaleString('es-ES', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
            useGrouping: true // Para usar separadores de miles
        });
    } catch (error) {
        console.error('Error al formatear número:', error);
        return num.toString();
    }
}


//  Función para guardar transacción
async function guardarTransaccion(isIncome) {
    const prefix = isIncome ? 'Ingreso' : 'Gasto';
    const formId = isIncome ? 'ingresoForm' : 'gastoForm';
    const form = document.getElementById(formId);

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    // Obtener y formatear los datos del formulario
    const monto = parseFloat(document.getElementById(`monto${prefix}`).value.replace(/[^\d.-]/g, ''));
    const descripcion = document.getElementById(`descripcion${prefix}`).value.trim();
    const categoriaId = parseInt(document.getElementById(`categoria${prefix}`).value, 10);
    const fechaInput = document.getElementById(`fecha${prefix}`).value;

    // Formatear la fecha correctamente para el backend
    // Asegurarse de que la fecha esté en formato ISO
    const fecha = new Date(fechaInput).toISOString().slice(0, 19).replace('T', ' ');

    const transactionData = {
        value: isIncome ? Math.abs(monto) : -Math.abs(monto),
        description: descripcion,
        categoryId: categoriaId,
        date: fecha,
    };

    try {
        const headers = {
            'Content-Type': 'application/json',
        };

        if (token && headerName) {
            headers[headerName] = token;
        }

        const response = await fetch("/finance/transactions", {
            method: "POST",
            headers: headers,
            body: JSON.stringify(transactionData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            console.error("Error en la respuesta del servidor:", errorData);
            throw new Error(errorData.error || "Error inesperado");
        }

        const savedTransaction = await response.json();

        // Cerrar el modal y resetear el formulario
        const modalId = isIncome ? 'ingresoModal' : 'gastoModal';
        const modal = bootstrap.Modal.getInstance(document.getElementById(modalId));
        modal.hide();
        form.reset();

        // Actualizar la interfaz
        await actualizarInterfaz(savedTransaction);

    } catch (error) {
        console.error("Error al guardar la transacción:", error);
        alert(`Error al guardar la transacción: ${error.message}`);
    }
}

async function actualizarInterfaz(savedTransaction) {
    try {
        // Actualizar la tabla
        actualizarTablaTransacciones(savedTransaction);

        // Reaplicar formateos
        formatearTodasLasFechas();

        // Actualizar el balance
        await actualizarBalanceCompleto();
    } catch (error) {
        console.error("Error al actualizar la interfaz:", error);
    }
}



// Función para actualizar la tabla de transacciones
function actualizarTablaTransacciones(transaction) {
    const tableBody = document.getElementById('transactionsTableBody');
    if (!tableBody) return;

    const row = tableBody.insertRow(0);

    // Crear las celdas
    const cells = {
        value: row.insertCell(0),
        description: row.insertCell(1),
        category: row.insertCell(2),
        date: row.insertCell(3)
    };

    // Formatear y asignar los valores
    cells.value.textContent = formatNumber(transaction.value);
    cells.description.textContent = transaction.description;
    cells.category.textContent = transaction.category.name;

    // Formatear la fecha usando la función existente de formatDateToMonth
    const fecha = new Date(transaction.date);
    cells.date.textContent = formatDateToMonth(fecha.toISOString());

    // Aplicar clases de estilo
    cells.value.className = transaction.value >= 0 ? 'text-success' : 'text-danger';
}

// Función para actualizar el balance completo
async function actualizarBalanceCompleto() {
    try {
        const response = await fetch('/finance/transactions');
        if (!response.ok) {
            throw new Error('Error al obtener el balance');
        }

        const balance = await response.json();

        // Asignar y formatear los valores del balance
        const elementos = {
            'totalIngresos': balance.income,
            'totalGastos': Math.abs(balance.expenses),
            'saldoTotal': balance.total
        };

        // Actualizar los elementos con el formato deseado
        for (const [id, valor] of Object.entries(elementos)) {
            const elemento = document.getElementById(id);
            if (elemento) {
                elemento.textContent = `${formatNumber(valor)} $`;  // Formateamos y agregamos el símbolo de moneda
            }
        }
    } catch (error) {
        console.error('Error al actualizar el balance:', error);
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

function agruparPorMes(tableRows) {
    const totalesPorMes = {};

    // Convertir HTMLCollection a Array
    Array.from(tableRows).forEach(row => {
        const mes = row.cells[3].textContent.toLowerCase().trim();
        // Limpiamos el string de cualquier formato (puntos, comas, espacios y símbolos de moneda)
        const montoStr = row.cells[0].textContent
            .replace(/\./g, '') // Eliminar puntos de miles
            .replace(/,/g, '.') // Reemplazar coma decimal por punto
            .replace(/[^\d.-]/g, ''); // Eliminar todo excepto números, punto decimal y signo negativo

        const monto = parseFloat(montoStr);

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

        crearGraficasDashboard(ingresosPorMes, gastosPorMes)
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

// Y en las gráficas, asegurémonos de formatear los números correctamente
function crearGraficas(ingresosPorMes, gastosPorMes) {
    const ordenMeses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
        'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

    const meses = [...new Set([...Object.keys(ingresosPorMes), ...Object.keys(gastosPorMes)])];
    meses.sort((a, b) => {
        return ordenMeses.indexOf(a.toLowerCase()) - ordenMeses.indexOf(b.toLowerCase());
    });

    // Función auxiliar para formatear números en las gráficas
    const formatearNumeroGrafica = (valor) => {
        return new Intl.NumberFormat('es-ES', {
            style: 'decimal',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(valor);
    };

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
                            return '$' + formatearNumeroGrafica(value);
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return '$' + formatearNumeroGrafica(context.raw);
                        }
                    }
                }
            }
        }
    });

    // Gráfico de Gastos (mismo patrón que el de Ingresos)
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
                            return '$' + formatearNumeroGrafica(value);
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return '$' + formatearNumeroGrafica(context.raw);
                        }
                    }
                }
            }
        }
    });
}


document.addEventListener('DOMContentLoaded', formatearTodasLasFechas);

// Al cargar la página, formatear los números en la tabla
document.addEventListener('DOMContentLoaded', function() {
    // Formatear todos los números en la tabla al cargar
    const numberCells = document.querySelectorAll('#transactionsTableBody td:first-child');
    numberCells.forEach(cell => {
        const value = parseFloat(cell.textContent);
        if (!isNaN(value)) {
            cell.textContent = formatNumber(value);
            cell.className = value >= 0 ? 'text-success' : 'text-danger';
        }
    });

    // Formatear las fechas al cargar
    const dateCells = document.querySelectorAll('#transactionsTableBody td:nth-child(4)'); // Asumiendo que la fecha está en la 4ª columna
    dateCells.forEach(cell => {
        const formattedDate = formatDateToMonth(cell.textContent);
        cell.textContent = formattedDate;
    });
});

// Función para convertir fecha ISO a nombre de mes en español
function convertirFechaAMes(fecha) {
    const meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
        'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

    // Asumir que 'fecha' es una cadena en formato ISO
    const date = new Date(fecha);

    // Asegúrate de que la fecha sea válida antes de usar getMonth
    if (isNaN(date.getTime())) {
        console.error("Fecha inválida:", fecha);
        return null;
    }

    return meses[date.getMonth()];
}

// Función para agregar valores por mes
function agregarValoresPorMes(datosServer, objetoDestino) {
    Object.entries(datosServer).forEach(([fecha, valor]) => {
        const mes = convertirFechaAMes(fecha);
        if (mes) {
            objetoDestino[mes] = (objetoDestino[mes] || 0) + valor;
        }
    });
}

// Inicializar los objetos
const ingresosPorMes = {};
const gastosPorMes = {};

// Agregar los valores a los objetos
agregarValoresPorMes(ingresosPorMesServer, ingresosPorMes);
agregarValoresPorMes(gastosPorMesServer, gastosPorMes);

// Colores para el gráfico de dona
const colors = [
    'rgba(255, 99, 132, 0.7)',
    'rgba(6,129,196,0.7)',
    'rgba(255, 206, 86, 0.7)',
    'rgba(75, 192, 192, 0.7)',
    'rgba(153, 102, 255, 0.7)',
    'rgba(255, 159, 64, 0.7)',
    'rgba(199, 199, 199, 0.7)',
    'rgba(6,129,196,0.7)'
];

// Crear las gráficas al cargar el DOM
document.addEventListener('DOMContentLoaded', function() {
    // Crear gráfica de ingresos y gastos
    crearGraficasDashboard(ingresosPorMes, gastosPorMes);

    // Crear gráfico de dona para gastos por categoría
    const spendingCtx = document.getElementById('spendingDonut').getContext('2d');
    const labels = Object.keys(gastosPorCategoria);
    const data = Object.values(gastosPorCategoria).map(value => parseFloat(value));

    new Chart(spendingCtx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors.slice(0, labels.length),
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = context.raw;
                            return new Intl.NumberFormat('es-ES', {
                                style: 'currency',
                                currency: 'EUR'
                            }).format(value);
                        }
                    }
                }
            }
        }
    });
});

async function addPayment() {
    const paymentName = document.getElementById("paymentName").value.trim();
    const paymentAmount = parseFloat(document.getElementById("paymentAmount").value);

    if (!paymentName || isNaN(paymentAmount)) {
        alert("Por favor completa todos los campos correctamente.");
        return;
    }

    const paymentData = {
        name: paymentName,
        value: Number(paymentAmount.toFixed(2))
    };

    try {
        const headers = {
            "Content-Type": "application/json",
            [headerName]: token
        };

        const response = await fetch("/finance/dashboard", {
            method: "POST",
            headers: headers,
            body: JSON.stringify(paymentData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error response:", {
                status: response.status,
                statusText: response.statusText,
                body: errorText
            });
            throw new Error(errorText || `Error ${response.status}: ${response.statusText}`);
        }

        const newPayment = await response.json();
        if (!newPayment || !newPayment.name || !newPayment.value) {
            throw new Error('Respuesta del servidor inválida');
        }

        addPaymentToList(newPayment);
        document.getElementById("addPaymentForm").reset();
        const addPaymentModal = bootstrap.Modal.getInstance(document.getElementById("addPaymentModal"));
        addPaymentModal.hide();
    } catch (error) {
        console.error("Error completo:", error);
        alert(`Error al agregar el pago: ${error.message}`);
    }
}

function addPaymentToList(payment) {
    const paymentList = document.getElementById("paymentList");

    const newPayment = document.createElement("li");
    newPayment.classList.add("list-group-item", "d-flex", "justify-content-between", "align-items-center");
    newPayment.innerHTML = `
        ${payment.name}
        <span class="badge bg-danger">-$${payment.value.toFixed(2)}</span>
    `;
    paymentList.appendChild(newPayment);
}




