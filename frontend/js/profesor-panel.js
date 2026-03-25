// 1. Validar el token y los permisos
const token = localStorage.getItem('token');
if (!token) window.location.href = 'index.html';

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