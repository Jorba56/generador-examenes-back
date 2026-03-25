document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const correo_usuario = document.getElementById('email').value;
    const contrasenha_usuario = document.getElementById('password').value;

    try {
        // Asegúrate de que esta es la ruta correcta de tu controlador de Login
        const response = await fetch('http://localhost:8080/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ correo_usuario, contrasenha_usuario })
        });

        if (response.ok) {
            // Dependiendo de tu backend, el token puede venir en texto plano o en un JSON
            // Ajusta esto si tu backend devuelve un texto directamente ( await response.text() )
            const data = await response.json();
            const token = data.token || data.accessToken || data; // Blindaje

            // 1. Guardamos el token
            localStorage.setItem('token', token);

            // 2. MAGIA: Decodificamos y redirigimos
            redirigirSegunRol(token);

        } else {
            alert("Credenciales incorrectas. Revisa tu correo o contraseña.");
        }
    } catch (error) {
        alert("Fallo de conexión con el servidor.");
    }
});

// --- FUNCIÓN PARA LEER EL TOKEN Y REDIRIGIR ---
function redirigirSegunRol(token) {
    try {
        // 1. Extraemos el Payload (la parte central del token separada por puntos)
        const base64Url = token.split('.')[1];

        // 2. Arreglamos los caracteres especiales para que JS pueda decodificarlo
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        // 3. Lo convertimos a un objeto JavaScript
        const payloadDecodificado = JSON.parse(jsonPayload);
        console.log("Token decodificado:", payloadDecodificado); // Te servirá para cotillear en F12

        // 4. Buscamos el rol. Lo pasamos a mayúsculas para buscar fácilmente
        const rolesString = JSON.stringify(payloadDecodificado).toUpperCase();

        // 5. EL CRUCE DE CAMINOS
        if (rolesString.includes('ADMIN')) {
            window.location.href = 'admin-panel.html';
        } else if (rolesString.includes('PROFESOR')) {
            window.location.href = 'profesor-panel.html';
        } else if (rolesString.includes('ALUMNO')) {
            window.location.href = 'alumno-panel.html';
        } else {
            alert("Tu usuario no tiene un rol válido asignado.");
            localStorage.removeItem('token');
        }

    } catch (error) {
        console.error("Error al parsear el token", error);
        alert("Error de seguridad al procesar tus datos. Inicia sesión de nuevo.");
        localStorage.removeItem('token');
    }
}