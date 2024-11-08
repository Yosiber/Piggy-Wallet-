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