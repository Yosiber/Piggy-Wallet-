const token = document.querySelector("meta[name='_csrf']").getAttribute('content');
const headerName = document.querySelector("meta[name='_csrf_header']").getAttribute('content');
console.log('Token disponible:', !!token);
console.log('Nombre del header disponible:', !!headerName);

function deleteUser(userId) {
    if (confirm('¿Estás seguro de que deseas eliminar este usuario?')) {
        // Obtener el token CSRF y el nombre del header
        const token = document.querySelector("meta[name='_csrf']").content;
        const headerName = document.querySelector("meta[name='_csrf_header']").content;

        // Crear el objeto headers
        const headers = {
            'Content-Type': 'application/json'
        };
        // Añadir el token CSRF al header
        headers[headerName] = token;

        fetch(`/admin/users/${userId}/delete`, {
            method: 'POST',
            headers: headers,
            credentials: 'same-origin' // Importante para incluir las cookies
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.error) {
                    alert(data.error);
                } else {
                    alert(data.message);
                    location.reload();
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al eliminar el usuario');
            });
    }
}