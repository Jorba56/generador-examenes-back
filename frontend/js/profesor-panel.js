// 1. Validar el token y los permisos
const token = localStorage.getItem('token');
if (!token) window.location.href = 'index.html';
const API_URL="http://localhost:8080";

try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(decodeURIComponent(window.atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join('')));

    const roles = JSON.stringify(payload).toUpperCase();

    // Protección: Si no es Profesor ni Admin, a la calle
    if (!roles.includes('PROFESOR') && !roles.includes('ADMIN')) {
        alert("Acceso denegado. No tienes permisos de docente.");
        localStorage.removeItem('token');
        window.location.href = 'index.html';
    }

    // Mostramos el nombre/correo
    document.getElementById('usuario-info').innerText = payload.sub || payload.correo_usuario;

} catch (e) {
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

// 2. Control del menú lateral
function cargarVistaProfesor(url, elemento, titulo) {
    document.getElementById('contenido-profesor').src = url;

    // Resetear colores del menú
    document.querySelectorAll('nav a').forEach(a => {
        a.classList.remove('bg-indigo-800', 'border-indigo-400');
        a.classList.add('border-transparent');
    });

    // Marcar el activo
    elemento.classList.remove('border-transparent');
    elemento.classList.add('bg-indigo-800', 'border-indigo-400');

    // Cambiar título
    document.getElementById('titulo-pagina').innerText = titulo;
}

function cerrarSesion() {
    localStorage.removeItem('token');
    window.top.location.href = 'index.html';
}

let idUsuarioActual = null; // Guardamos la ID aquí para cuando le dé a "Guardar"

// --- ABRIR MODAL DE MI PERFIL ---
window.abrirModalMiPerfil = async function() {
    // 1. Sacamos el correo directamente del Token (100% fiable)
    const token = localStorage.getItem('token');
    const payload = JSON.parse(atob(token.split('.')[1]));
    const miCorreo = payload.sub || payload.correo_usuario;

    try {
        const responseBusqueda = await fetch(`${API_URL}/usuarios/email/${miCorreo}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (responseBusqueda.ok) {
            const usuario = await responseBusqueda.json();

            // 3. ¡LA MAGIA! A raíz del usuario encontrado, atrapamos su ID
            idUsuarioActual = usuario.id_usuario;

            // 4. Rellenamos el formulario con los datos frescos
            document.getElementById('miNombre').value = usuario.nombre_usuario || usuario.nombreUsuario || '';
            document.getElementById('miApellidos').value = usuario.apellido_usuario || usuario.apellidoUsuario || '';
            document.getElementById('miCorreo').value = usuario.correo_usuario || usuario.emailUsuario || miCorreo;
            document.getElementById('miPassword').value = '';

            // Mostramos la ventana
            document.getElementById('modalMiPerfil').classList.remove('hidden');

        } else {
            alert("No se pudo encontrar tu perfil usando el correo: " + miCorreo);
        }
    } catch (error) {
        alert("Fallo de red al buscar el usuario por correo.");
    }
}

// --- CERRAR MODAL ---
window.cerrarModalMiPerfil = function() {
    document.getElementById('modalMiPerfil').classList.add('hidden');
    if (idUsuarioActual!= null){
        idUsuarioActual = null; // Limpiamos por seguridad
    }
}

// --- GUARDAR LOS CAMBIOS PROPIOS (PUT) ---
window.guardarMiPerfil = async function() {
    if (!idUsuarioActual) {
        return alert("Error: No se ha podido capturar tu ID para actualizar el perfil.");
    }

    const payload = {
        nombre_usuario: document.getElementById('miNombre').value,
        apellido_usuario: document.getElementById('miApellidos').value,
        correo_usuario: document.getElementById('miCorreo').value,
        contrasenha_usuario: document.getElementById('miPassword').value
    };

    try {
        // 5. Usamos la ID que capturamos antes para hacer el PUT
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
                alert("Has cambiado tu contraseña. Recuerda usar la nueva la próxima vez que inicies sesión.");
            }
            cerrarModalMiPerfil();
        } else {
            alert(`Error del Servidor al actualizar (${response.status}).`);
        }
    } catch (error) {
        alert("Fallo de conexión al guardar.");
    }
}