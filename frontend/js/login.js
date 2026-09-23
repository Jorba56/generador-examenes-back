const API_URL = 'http://localhost:8080';

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const correo_usuario = document.getElementById('email').value;
    const contrasenha_usuario = document.getElementById('password').value;

    try {
        // 1. PETICIÓN DE LOGIN
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ correo_usuario, contrasenha_usuario })
        });

        if (response.ok) {
            const data = await response.json();
            // Sacamos el token (manejando posibles nombres de variable del backend)
            const token = data.token || data.accessToken || data;

            if (!token) throw new Error("No se ha recibido el token del servidor.");

            // 2. GUARDAMOS EL TOKEN INICIAL
            localStorage.setItem('token', token);

            // 4. REDIRIGIR SEGÚN ROL
            redirigirSegunRol(token);

        } else {
            document.getElementById('errorMessage').classList.remove('hidden');
            alert("Credenciales incorrectas.");
        }
    } catch (error) {
        console.error("Error en el proceso de login:", error);
        alert("Fallo de conexión con el servidor.");
    }
});

/**
 * Función auxiliar para obtener el ID del usuario.
 * Se ejecuta durante el login para dejar el ID listo en el navegador.
 */
async function obtenerIdPorCorreo(correo, token) {
    try {
        const response = await fetch(`${API_URL}/usuarios/email/${correo}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const usuario = await response.json();
            const idEncontrado = usuario.id_user ||usuario.idUser;
            console.log("ID de usuario recuperado:", idEncontrado);
            return idEncontrado;
        }
        return 0; // Si no lo encuentra, devolvemos 0 para que el back guarde 0L
    } catch (error) {
        console.error("Error al obtener ID:", error);
        return 0;
    }
}

function redirigirSegunRol(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const payloadDecodificado = JSON.parse(jsonPayload);
        const rolesString = JSON.stringify(payloadDecodificado).toUpperCase();

        if (rolesString.includes('ADMIN')) {
            window.location.href = 'admin-panel.html';
        } else if (rolesString.includes('PROFESOR')) {
            window.location.href = 'profesor-panel.html';
        } else if (rolesString.includes('ALUMNO')) {
            window.location.href = 'alumno-panel.html';
        } else {
            alert("Tu usuario no tiene un rol válido.");
            localStorage.clear();
        }

    } catch (error) {
        console.error("Error al procesar el token:", error);
        alert("Error de seguridad. Inicia sesión de nuevo.");
        localStorage.clear();
    }
}