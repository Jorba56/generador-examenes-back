// 1. EL GUARDIÁN: Comprobar si hay token. Si no, al login.
const token = localStorage.getItem('token');
if (!token) {
    window.top.location.href = 'index.html';
}

// 2. CONFIGURACIÓN
const API_URL = 'http://localhost:8080'; // Apuntamos al API Gateway

// 3. CERRAR SESIÓN
document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.removeItem('token'); // Borramos el token
    window.top.location.href = 'index.html';
});

// --- VARIABLES DE ESTADO PARA PAGINACIÓN ---
let paginaActual = 0;
const tamanoPagina = 2;
let sortCampo = 'id';
let sortDireccion = 'desc';

// --- FUNCIONES DE CONTROL ---
window.cambiarOrden = function() {
    const select = document.getElementById('sortExamenes').value; // ej: "titulo-asc"
    const partes = select.split('-');
    sortCampo = partes[0];
    sortDireccion = partes[1];
    paginaActual = 0; // Volvemos a la página 1 al cambiar el orden
    cargarExamenes();
}

window.cambiarPagina = function(direccion) {
    paginaActual += direccion;
    cargarExamenes();
}

// --- CARGA DINÁMICA ---
async function cargarExamenes() {
    const tabla = document.getElementById('tablaExamenes');
    tabla.innerHTML = '<tr><td colspan="5" class="px-5 py-8 text-center text-gray-500 animate-pulse">Cargando exámenes...</td></tr>';

    try {
        // Usamos nuestras variables dinámicas en la URL
        const urlPaginada = `${API_URL}/examenes/paginados?page=${paginaActual}&size=${tamanoPagina}&sortBy=${sortCampo}&sortDir=${sortDireccion}`;

        const response = await fetch(urlPaginada, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            alert("Tu sesión ha caducado.");
            localStorage.removeItem('token');
            window.location.href = 'index.html';
            return;
        }

        if (response.ok) {
            const data = await response.json();
            const examenes = data.content || [];

            // --- MAGIA ANTI-UNDEFINED (Soporte Spring Boot 3+) ---
            // Buscamos los datos de paginación en la raíz o dentro del objeto "page"
            const meta = data.page || data;
            const numPag = meta.number !== undefined ? meta.number : 0;
            const totalPag = meta.totalPages || 1;
            const totalElem = meta.totalElements !== undefined ? meta.totalElements : examenes.length;

            // Calculamos manualmente si es la primera o la última página
            const esPrimera = data.first !== undefined ? data.first : (numPag === 0);
            const esUltima = data.last !== undefined ? data.last : (numPag >= totalPag - 1);

            document.getElementById('infoPaginacion').innerText = `Página ${numPag + 1} de ${totalPag} (Total: ${totalElem})`;
            document.getElementById('btnAnt').disabled = esPrimera;
            document.getElementById('btnSig').disabled = esUltima;

            tabla.innerHTML = '';

            if (examenes.length === 0) {
                tabla.innerHTML = `<tr><td colspan="5" class="px-5 py-5 text-center font-bold text-gray-500">No hay exámenes registrados.</td></tr>`;
                return;
            }

            examenes.forEach(examen => {
                const fechaFormat = examen.fecha_creacion ? new Date(examen.fecha_creacion).toLocaleDateString() : 'N/A';

                tabla.innerHTML += `
                    <tr class="hover:bg-gray-50 transition duration-150">
                        <td class="px-5 py-4 border-b border-gray-200 text-sm">#${examen.id}</td>
                        <td class="px-5 py-4 border-b border-gray-200 text-sm font-bold text-gray-900">${examen.titulo || 'Sin título'}</td>
                        <td class="px-5 py-4 border-b border-gray-200 text-sm">${fechaFormat}</td>
                        <td class="px-5 py-4 border-b border-gray-200 text-sm text-center">
                            <span class="bg-blue-100 text-blue-800 py-1 px-3 rounded-full text-xs font-bold">
                                ${examen.numero_preguntas}
                            </span>
                        </td>
                        <td class="px-5 py-4 border-b border-gray-200 text-sm text-center">
                            <button onclick="verDetalles(${examen.id})" class="text-blue-600 hover:text-blue-900 font-semibold mr-3">Ver</button>
                            <button onclick="borrarExamen(${examen.id})" class="text-red-600 hover:text-red-900 font-semibold">Borrar</button>
                        </td>
                    </tr>
                `;
            });
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="5" class="px-5 py-5 text-center text-red-500 font-bold">Fallo de conexión.</td></tr>`;
    }
}

// Función dummy para los botones (por ahora solo hace un alert)
// js/dashboard.js (al final del archivo)

// Función para manejar el clic en el botón "Ver" de la tabla
window.verDetalles = function(idExamen) {
    // Redirigimos a la nueva página pasando el ID por la URL como parámetro 'id'
    window.location.href = `examen-detalle.html?id=${idExamen}`;
}

window.borrarExamen = async function(idExamen) {
    // 1. Pedimos confirmación para evitar borrados accidentales
    const confirmar = confirm(`¿Estás seguro de que quieres borrar el examen #${idExamen}? Esta acción no se puede deshacer.`);

    if (!confirmar) {
        return; // Si el usuario cancela, no hacemos nada
    }

    try {
        // 2. Hacemos la llamada DELETE al API Gateway
        const response = await fetch(`${API_URL}/examenes/${idExamen}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            // 3. Si se borra bien, avisamos y RECARGAMOS la tabla automáticamente
            alert("Examen eliminado correctamente.");
            cargarExamenes();
        } else if (response.status === 403) {
            // Capturamos el error de Spring Security si un ALUMNO intenta borrar
            alert("No tienes permisos para borrar exámenes. Solo los profesores pueden hacerlo.");
        } else {
            const errorTxt = await response.text();
            alert("Error al borrar el examen: " + errorTxt);
        }
    } catch (error) {
        console.error("Fallo de conexión:", error);
        alert("Fallo de conexión con el servidor al intentar borrar.");
    }
}

// Arrancamos la petición al cargar el JS
cargarExamenes();