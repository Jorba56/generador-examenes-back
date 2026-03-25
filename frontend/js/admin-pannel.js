
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