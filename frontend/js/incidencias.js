const token = localStorage.getItem('token');
if (!token) window.location.href = 'index.html';

const API_URL = 'http://localhost:8080';
let incidenciasGlobales = [];

// --- 1. LÓGICA DE LOS FILTROS (Buscador) ---
document.getElementById('filtroTipo').addEventListener('change', function() {
    const inputValor = document.getElementById('filtroValor');
    if (this.value === 'todas') {
        inputValor.classList.add('hidden');
        inputValor.value = ''; // Limpiamos el texto
    } else {
        inputValor.classList.remove('hidden');
        inputValor.placeholder = `Escribe el valor para ${this.value}...`;
    }
});

window.aplicarFiltro = function() {
    const tipo = document.getElementById('filtroTipo').value;
    const valor = document.getElementById('filtroValor').value.trim();

    let urlFetch = `${API_URL}/incidencias`; // Por defecto, el de las últimas 50

    if (tipo !== 'todas') {
        if (!valor) return alert("Por favor, escribe un valor para filtrar.");
        urlFetch = `${API_URL}/incidencias/${tipo}/${valor}`;
    }

    cargarIncidencias(urlFetch);
}

// --- 2. FUNCIÓN PRINCIPAL DE CARGA DE LA TABLA ---
window.cargarIncidencias = async function(urlPersonalizada = null) {
    const tabla = document.getElementById('tablaIncidencias');
    tabla.innerHTML = '<tr><td colspan="5" class="text-center py-8 text-blue-500 animate-pulse">Cargando incidencias...</td></tr>';

    // Si pasamos una URL por el filtro, la usa. Si no, usa la general.
    const urlFinal = (typeof urlPersonalizada === 'string') ? urlPersonalizada : `${API_URL}/incidencias`;

    try {
        const response = await fetch(urlFinal, {
            headers: { 'Authorization': `Bearer ${token}`}
        });

        if (response.ok) {
            const data = await response.json();
            incidenciasGlobales = data.content ? data.content : data;

            tabla.innerHTML = '';

            if (incidenciasGlobales.length === 0) {
                tabla.innerHTML = `<tr><td colspan="5" class="text-center py-8 text-green-600 font-bold"><i class="fas fa-check-circle mr-2"></i>No se han encontrado incidencias.</td></tr>`;
                return;
            }

            incidenciasGlobales.forEach(inc => {
                const fechaFormat = new Date(inc.fecha).toLocaleString();
                // Blindaje del ID para que no falle el botón de Traza
                const idReal = inc.idIncidencia || inc.id_incidencia || inc.id;

                tabla.innerHTML += `
                    <tr class="hover:bg-red-50 border-b border-gray-100 transition">
                        <td class="px-5 py-3 text-sm">
                            <span class="font-bold text-gray-800">#${idReal}</span><br>
                            <span class="text-xs text-gray-500">${fechaFormat}</span>
                        </td>
                        <td class="px-5 py-3 text-sm font-mono text-blue-600">${inc.endpoint || 'N/A'}</td>
                        <td class="px-5 py-3 text-sm">
                            <span class="bg-red-100 text-red-800 text-xs font-bold px-2 py-1 rounded">${inc.clase}</span>
                        </td>
                        <td class="px-5 py-3 text-sm text-center text-gray-600">${inc.idUsuario || inc.id_usuario || 'Anónimo'}</td>
                        <td class="px-5 py-3 text-sm text-center">
                            <button onclick="abrirModal(${idReal})" class="bg-gray-100 text-gray-800 hover:bg-gray-200 border border-gray-300 font-bold py-1 px-3 rounded shadow-sm">
                                <i class="fas fa-eye mr-1"></i> Traza
                            </button>
                        </td>
                    </tr>
                `;
            });
        } else {
            tabla.innerHTML = `<tr><td colspan="5" class="text-center py-8 text-red-500">Error al cargar las incidencias (Status: ${response.status})</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="5" class="text-center py-8 text-red-500 font-bold">Fallo de conexión con el servicio de incidencias.</td></tr>`;
    }
}

// --- 3. FUNCIONES DEL MODAL ---
window.abrirModal = function(idIncidencia) {
    const incidencia = incidenciasGlobales.find(inc => (inc.idIncidencia || inc.id_incidencia || inc.id) === idIncidencia);

    if (incidencia) {
        document.getElementById('modalTitulo').innerText = `Detalles del Error #${idIncidencia}`;
        document.getElementById('modalTipo').innerText = incidencia.tipo || 'Desconocido';
        document.getElementById('modalMetodo').innerText = incidencia.metodo || 'Desconocido';
        document.getElementById('modalTextoTraza').innerText = incidencia.traza || 'No hay traza disponible.';

        document.getElementById('modalTraza').classList.remove('hidden');
    }
}

window.cerrarModal = function() {
    document.getElementById('modalTraza').classList.add('hidden');
}

// Arrancamos la tabla al abrir la página
cargarIncidencias();