
(function protegerRuta() {
    const token = localStorage.getItem('token');

    // Si no hay token, fuera
    if (!token) {
        window.location.replace('login.html');
        return;
    }

    try {
        // Abrimos el token para cotillear los roles
        const payload = JSON.parse(atob(token.split('.')[1]));
        const rolesString = JSON.stringify(payload).toUpperCase();

        // Si es el panel de admin y NO tiene el rol ADMIN, fuera
        if (!rolesString.includes('ADMIN')) {
            alert("Acceso denegado: No tienes permisos de Administrador.");
            window.location.replace('login.html'); // O mándalo a su panel correspondiente
        }
    } catch (e) {
        // Si el token está corrupto o lo han modificado a mano, fuera
        localStorage.removeItem('token');
        window.location.replace('login.html');
    }
})();

// Validar seguridad global
const token = localStorage.getItem('token');
if (!token) {
    window.location.href = 'index.html';
}

// Cerrar sesión desde el panel principal
document.getElementById('btnLogout').addEventListener('click', () => {
    localStorage.removeItem('token');
    window.location.href = 'index.html';
});

// Función para cambiar de pantalla en el iframe
window.cargarVista = function(url, elementoHtml, titulo) {
    // 1. Cambiamos la URL del iframe central
    document.getElementById('iframeContenido').src = url;

    // 2. Actualizamos el título de arriba
    document.getElementById('tituloSeccion').innerText = titulo;

    // 3. Quitamos el color activo (azul) de todos los botones del menú
    document.querySelectorAll('.menu-item').forEach(el => {
        el.classList.remove('bg-gray-800', 'border-blue-500');
        el.classList.add('border-transparent');
    });

    // 4. Le ponemos el color activo al botón que acabamos de pulsar
    elementoHtml.classList.add('bg-gray-800', 'border-blue-500');
    elementoHtml.classList.remove('border-transparent');
}