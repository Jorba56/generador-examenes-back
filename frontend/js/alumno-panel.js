// 1. VARIABLES GLOBALES
const API_URL = "http://localhost:8080"; // Faltaba el 'const' aquí
let idUsuarioActual = null;

// 2. LÓGICA DE INICIO Y PROTECCIÓN (Se ejecuta al cargar la página)
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');

    // Si no hay token, a la calle directo
    if (!token) {
        window.location.replace('index.html');
        return;
    }

    try {
        // Decodificamos el token de forma segura
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const payload = JSON.parse(jsonPayload);
        const roles = JSON.stringify(payload).toUpperCase();

        // Protección: Si no es ALUMNO, a la calle
        if (!roles.includes('ALUMNO')) {
            alert("Acceso denegado. No tienes permisos de alumno para ver este panel.");
            localStorage.removeItem('token');
            window.location.replace('index.html');
            return;
        }

        // Mostrar el correo del alumno en la cabecera
        const userInfo = document.getElementById('usuario-info');
        if (userInfo) {
            userInfo.innerText = payload.sub || payload.correo_usuario;
        }

    } catch (error) {
        console.error("Error leyendo el token de seguridad.");
        localStorage.removeItem('token');
        window.location.replace('index.html');
    }
});

// 3. FUNCIONES DE LA INTERFAZ (Disponibles para el HTML)

window.cargarVistaAlumno = function(url, elemento) {
    document.getElementById('contenido-alumno').src = url;
    document.querySelectorAll('nav a').forEach(a => a.classList.remove('bg-blue-800', 'border-blue-400'));
    elemento.classList.add('bg-blue-800', 'border-blue-400');
    document.getElementById('titulo-pagina').innerText = elemento.innerText.trim();
};

window.cerrarSesion = function() {
    localStorage.removeItem('token');
    window.top.location.replace('index.html');
};

// --- ABRIR MODAL DE MI PERFIL ---
window.abrirModalMiPerfil = async function() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const miCorreo = payload.sub || payload.correo_usuario;

        const responseBusqueda = await fetch(`${API_URL}/usuarios/email/${miCorreo}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (responseBusqueda.ok) {
            const usuario = await responseBusqueda.json();

            // Atrapamos la ID
            idUsuarioActual = usuario.id_usuario || usuario.idUser || usuario.id;

            // Rellenamos el formulario
            document.getElementById('miNombre').value = usuario.nombre_usuario || usuario.nombreUsuario || '';
            document.getElementById('miApellidos').value = usuario.apellido_usuario || usuario.apellidoUsuario || '';
            document.getElementById('miCorreo').value = usuario.correo_usuario || usuario.emailUsuario || miCorreo;
            document.getElementById('miPassword').value = '';

            // Mostramos la ventana
            document.getElementById('modalMiPerfil').classList.remove('hidden');
        } else {
            alert("No se pudo cargar tu perfil.");
        }
    } catch (error) {
        alert("Fallo de red al buscar el perfil.");
    }
};

// --- CERRAR MODAL ---
window.cerrarModalMiPerfil = function() {
    document.getElementById('modalMiPerfil').classList.add('hidden');
    idUsuarioActual = null; // Limpiamos por seguridad
};

// --- GUARDAR LOS CAMBIOS PROPIOS (PUT) ---
window.guardarMiPerfil = async function() {
    if (!idUsuarioActual) {
        return alert("Error: No se ha capturado tu ID. Cierra la ventana y vuelve a intentarlo.");
    }

    const payload = {
        nombre_usuario: document.getElementById('miNombre').value,
        apellido_usuario: document.getElementById('miApellidos').value,
        correo_usuario: document.getElementById('miCorreo').value,
        contrasenha_usuario: document.getElementById('miPassword').value
    };

    try {
        const response = await fetch(`${API_URL}/usuarios/${idUsuarioActual}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("¡Tu perfil ha sido actualizado con éxito!");
            if (payload.contrasenha_usuario) {
                alert("Has cambiado tu contraseña. Inicia sesión de nuevo.");
                cerrarSesion(); // Forzamos salir si cambia la clave
            } else {
                cerrarModalMiPerfil();
            }
        } else {
            alert(`Error del Servidor al actualizar (${response.status}).`);
        }
    } catch (error) {
        alert("Fallo de conexión al guardar.");
    }
};