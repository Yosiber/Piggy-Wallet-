document.addEventListener('DOMContentLoaded', function() {
    // Gráfico de barras
    var ctxBar = document.getElementById('myBarChartIncome').getContext('2d');
    var myBarChart = new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo'],
            datasets: [{
                label: 'Ingresos',
                data: [12000, 15000, 13000, 14000, 17000],
                backgroundColor: 'rgba(40, 167, 69, 0.5)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });


    // Gráfico de barras
    var ctxBar = document.getElementById('myBarChartSpending').getContext('2d');
    var myBarChart = new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo'],
            datasets: [{
                label: 'Gastos',
                data: [12000, 15000, 13000, 14000, 17000],
                backgroundColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });


});


document.addEventListener('DOMContentLoaded', function() {
    // Balance Summary Chart
    const balanceCtx = document.getElementById('balanceChart').getContext('2d');
    const balanceChart = new Chart(balanceCtx, {
        type: 'line',
        data: {
            labels: ['Mar 1', 'Mar 5', 'Mar 10', 'Mar 14'],
            datasets: [{
                label: 'Income',
                data: [1000, 2000, 1500, 2200],
                borderColor: 'rgba(75, 192, 192, 1)',
                fill: false
            }, {
                label: 'Spending',
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
            const modalId = isIncome ? 'ingresoModal' : 'gastoModal';
            const modal = bootstrap.Modal.getInstance(document.getElementById(modalId));
            modal.hide();
            form.reset();
            actualizarListaTransacciones(savedTransaction);
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

// Helper function to format the date by month and year
function formatDateToMonth(dateString) {
    const date = new Date(dateString);
    const options = { year: 'numeric', month: 'long' };
    return date.toLocaleDateString(undefined, options); // "November 2024"
}

// Function to update the transaction list on the page, grouped by month
function actualizarListaTransacciones(transaction) {
    console.log("Actualizando lista con nueva transacción:", transaction);

    const tableBody = document.getElementById("transactionsTableBody");

    // Get the formatted month for the transaction date
    const transactionMonth = formatDateToMonth(transaction.date);

    // Check if a table row already exists for this month
    let monthRow = document.getElementById(`month-${transactionMonth}`);
    if (!monthRow) {
        // Create a new row for this month if it doesn't exist
        monthRow = document.createElement("tr");
        monthRow.id = `month-${transactionMonth}`;
        monthRow.innerHTML = `
            <td colspan="4"><strong>${transactionMonth}</strong></td>
        `;
        tableBody.appendChild(monthRow);
    }

    // Add the new transaction row under the month row
    const newRow = document.createElement("tr");
    newRow.innerHTML = `
        <td>${transaction.value.toFixed(2)}</td>
        <td>${transaction.description}</td>
        <td>${transaction.category.name}</td>
        <td>${transaction.date}</td>
    `;
    tableBody.appendChild(newRow);
}





