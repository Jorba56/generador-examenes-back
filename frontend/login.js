// Capturamos el formulario
const loginForm = document.getElementById('loginForm');
const errorMessage = document.getElementById('errorMessage');

// Ajusta este puerto al puerto donde esté corriendo tu API Gateway o tu usuarios-service
const API_URL = 'http://localhost:8080';

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Evitamos que la página se recargue

    // Cogemos los valores de los inputs
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        // Hacemos la petición POST al backend
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                correo: email,
                password: password
            })
        });

        if (response.ok) {
            // Si el login es correcto, extraemos el token del JSON
            const data = await response.json();

            // Guardamos el token en el navegador para usarlo en las siguientes pantallas
            localStorage.setItem('token', data.token); // Ajusta 'data.token' según cómo devuelva el JSON tu backend

            // Ocultamos el error por si estaba visible
            errorMessage.classList.add('hidden');

            // Redirigimos a la pantalla principal
            window.location.href = 'dashboard.html';
        } else {
            // Si el backend devuelve 401 (Unauthorized) o error
            errorMessage.classList.remove('hidden');
        }
    } catch (error) {
        console.error("Error conectando con el servidor:", error);
        errorMessage.textContent = "Error de conexión con el servidor.";
        errorMessage.classList.remove('hidden');
    }
});