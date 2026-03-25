const token = localStorage.getItem('token');
if (!token) window.top.location.href = 'index.html';

const API_URL = 'http://localhost:8080';
let alumnosGlobales = [];

async function cargarAlumnos() {
    const tabla = document.getElementById('tablaAlumnos');

    try {
        const response = await fetch(`${API_URL}/alumnos`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            alumnosGlobales = await response.json();
            pintarTabla(alumnosGlobales);
        } else {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Error cargando los alumnos. Status: ${response.status}</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Fallo de red al conectar con el servidor de Usuarios.</td></tr>`;
    }
}

function pintarTabla(lista) {
    const tabla = document.getElementById('tablaAlumnos');
    tabla.innerHTML = '';

    if (lista.length === 0) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-10 text-gray-500 font-bold">No se encontraron alumnos.</td></tr>`;
        return;
    }

    // Leemos si el que mira es ADMIN o PROFESOR para mostrar o esconder la papelera
    const payload = JSON.parse(atob(token.split('.')[1]));
    const esAdmin = JSON.stringify(payload).toUpperCase().includes('ADMIN');

    lista.forEach(al => {
        const id = al.id || al.idUsuario || al.id_usuario || '-';
        const correo = al.correo || al.correoUsuario || al.correo_usuario || 'Sin correo';

        const botonBorrar = esAdmin
            ? `<button onclick="borrarAlumno('${correo}')" class="text-red-500 hover:text-red-800 font-bold bg-red-50 hover:bg-red-100 px-3 py-2 rounded transition shadow-sm" title="Dar de baja">
                   <i class="fas fa-trash-alt"></i>
               </button>`
            : '';

        tabla.innerHTML += `
            <tr class="hover:bg-indigo-50 border-b border-gray-100 transition duration-150">
                <td class="px-5 py-4 text-sm font-bold text-gray-500">#${id}</td>
                <td class="px-5 py-4 text-sm">
                    <div class="flex items-center">
                        <div class="bg-indigo-100 text-indigo-600 rounded-full h-8 w-8 flex items-center justify-center font-bold mr-3 shadow-sm">
                            <i class="fas fa-user-graduate"></i>
                        </div>
                        <span class="font-bold text-gray-800">${correo}</span>
                    </div>
                </td>
                <td class="px-5 py-4 text-sm text-center">
                    <span class="bg-green-100 text-green-800 text-xs font-bold px-3 py-1 rounded-full border border-green-200">ALUMNO</span>
                </td>
                <td class="px-5 py-4 text-sm text-center flex justify-center gap-2">
                    <button onclick="verEstadisticas('${correo}')" class="text-indigo-600 hover:text-indigo-900 font-bold bg-indigo-50 hover:bg-indigo-100 px-3 py-2 rounded transition shadow-sm" title="Ver Rendimiento">
                        <i class="fas fa-chart-line"></i>
                    </button>
                    ${botonBorrar}
                </td>
            </tr>
        `;
    });
}

// --- BUSCADOR ---
document.getElementById('buscadorAlumnos').addEventListener('input', function(e) {
    const texto = e.target.value.toLowerCase();
    const filtrados = alumnosGlobales.filter(al => {
        const correo = (al.correo || al.correoUsuario || al.correo_usuario || "").toLowerCase();
        return correo.includes(texto);
    });
    pintarTabla(filtrados);
});

// --- VER ESTADÍSTICAS (MODAL) ---
window.verEstadisticas = async function(correo) {
    document.getElementById('modalCorreo').innerText = correo;
    document.getElementById('modalEstadisticas').classList.remove('hidden');

    // Mostramos cargando
    document.getElementById('contenedorDatosModal').classList.add('hidden');
    document.getElementById('mensajeErrorModal').classList.add('hidden');

    try {
        const response = await fetch(`${API_URL}/evaluaciones/estadisticas/${correo}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });

        if (response.ok) {
            const stats = await response.json();

            // Imprimimos en consola para cotillear qué nombres exactos manda Java
            console.log("Estadísticas recibidas del servidor:", stats);

            // Blindaje total: Buscamos en formato camelCase (Java) o snake_case (JSON)
            const media = stats.nota_media !== undefined ? stats.nota_media : (stats.notaMedia || 0);
            const total = stats.total_examenes_realizados !== undefined ? stats.total_examenes_realizados : (stats.total_examenes_realizados || 0);
            const aprobados = stats.examenes_aprobados !== undefined ? stats.examenes_aprobados : (stats.aprobados || 0);
            const suspensos = stats.examenes_suspendidos !== undefined ? stats.examenes_suspendidos : (stats.examenes_suspendidos || 0);

            // Inyectamos los valores seguros en el HTML
            document.getElementById('modalMedia').innerText = media;
            document.getElementById('modalTotal').innerText = total;
            document.getElementById('modalAprobados').innerText = aprobados;
            document.getElementById('modalSuspensos').innerText = suspensos;

            document.getElementById('contenedorDatosModal').classList.remove('hidden');
        } else if (response.status === 404) {
            // Tu backend devuelve 404 si no ha hecho exámenes
            document.getElementById('mensajeErrorModal').classList.remove('hidden');
        } else {
            alert("Error al cargar las estadísticas del alumno.");
            cerrarModalEstadisticas();
        }
    } catch (error) {
        alert("Fallo de red al solicitar estadísticas.");
        cerrarModalEstadisticas();
    }

    window.cerrarModalEstadisticas = function () {
        document.getElementById('modalEstadisticas').classList.add('hidden');
    }

// --- EXPORTAR A EXCEL (Generado en JS) ---
    window.exportarExcelAlumnos = function () {
        if (alumnosGlobales.length === 0) {
            return alert("No hay alumnos para exportar.");
        }

        // Cabeceras del CSV
        let csvContent = "ID;Correo;Rol\n";

        // Recorremos la lista y rellenamos las filas
        alumnosGlobales.forEach(al => {
            const id = al.id || al.idUsuario || al.id_usuario || '';
            const correo = al.correo || al.correoUsuario || al.correo_usuario || '';
            csvContent += `${id};${correo};ALUMNO\n`;
        });

        // Creamos un Blob mágico para forzar la descarga en formato UTF-8 (para que lea bien los caracteres Excel)
        const blob = new Blob(["\uFEFF" + csvContent], {type: 'text/csv;charset=utf-8;'});
        const url = URL.createObjectURL(blob);

        const enlaceOculto = document.createElement("a");
        enlaceOculto.setAttribute("href", url);
        enlaceOculto.setAttribute("download", "listado_alumnos.csv");

        document.body.appendChild(enlaceOculto);
        enlaceOculto.click(); // Simulamos el clic
        document.body.removeChild(enlaceOculto); // Limpiamos la basura
    }

// --- BORRAR ALUMNO ---
    window.borrarAlumno = async function (correo) {
        if (!confirm(`¿Dar de baja definitivamente a: ${correo}?`)) return;
        try {
            const response = await fetch(`${API_URL}/alumnos/${correo}`, {
                method: 'DELETE',
                headers: {'Authorization': `Bearer ${token}`}
            });
            if (response.ok) {
                alert("Alumno eliminado.");
                cargarAlumnos();
            } else {
                alert(`Error al eliminar (Status: ${response.status}).`);
            }
        } catch (error) {
            alert("Fallo de red.");
        }
    }
}
cargarAlumnos();