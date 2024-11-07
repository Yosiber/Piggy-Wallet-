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


function addCategory() {
    var categoryName = document.getElementById('categoryName').value;

    if (categoryName) {
        // Crear la nueva categoría en la interfaz
        var activeTab = document.querySelector('.tab-pane.active');
        var newCategory = document.createElement('li');
        newCategory.classList.add('list-group-item');
        newCategory.textContent = categoryName;
        activeTab.querySelector('.list-group').appendChild(newCategory);

        // Limpiar el campo de entrada y cerrar el modal
        document.getElementById('categoryName').value = '';
        var modal = bootstrap.Modal.getInstance(document.getElementById('addCategoryModal'));
        modal.hide();

        // Enviar la nueva categoría al backend
        fetch('/finance/categories', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ catName: categoryName })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al guardar la categoría');
                }
                return response.json();
            })
            .then(data => {
                console.log('Categoría guardada:', data);
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Hubo un error al guardar la categoría en el servidor.');
            });
    } else {
        alert('Por favor, ingrese un nombre para la categoría');
    }
}
