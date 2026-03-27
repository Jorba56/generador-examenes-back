const token = localStorage.getItem('token');
if (!token) window.top.location.href = 'index.html';

const API_URL = 'http://localhost:8080';
let alumnosGlobales = [];

// 1. CARGAR ALUMNOS (Al inicio)
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
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Fallo de red al conectar con el servidor.</td></tr>`;
    }
}

// 2. PINTAR TABLA
function pintarTabla(lista) {
    const tabla = document.getElementById('tablaAlumnos');
    tabla.innerHTML = '';

    if (lista.length === 0) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-10 text-gray-500 font-bold">No se encontraron alumnos.</td></tr>`;
        return;
    }

    const payload = JSON.parse(atob(token.split('.')[1]));
    const esAdmin = JSON.stringify(payload).toUpperCase().includes('ADMIN');

    lista.forEach(al => {
        const id = al.id || al.idUsuario || al.id_usuario || '-';
        const correo = al.correo || al.correoUsuario || al.correo_usuario || 'Sin correo';

        const botonBorrar = esAdmin
            ? `<button onclick="borrarAlumno('${correo}')" class="text-red-500 hover:text-red-800 font-bold bg-red-50 hover:bg-red-100 px-3 py-2 rounded transition shadow-sm">
                   <i class="fas fa-trash-alt"></i>
               </button>`
            : '';

        tabla.innerHTML += `
            <tr class="hover:bg-indigo-50 border-b border-gray-100 transition duration-150">
                <td class="px-5 py-4 text-sm font-bold text-gray-500">#${id}</td>
                <td class="px-5 py-4 text-sm font-bold text-gray-800">${correo}</td>
                <td class="px-5 py-4 text-sm text-center">
                    <span class="bg-green-100 text-green-800 text-xs font-bold px-3 py-1 rounded-full border border-green-200">ALUMNO</span>
                </td>
                <td class="px-5 py-4 text-sm text-center flex justify-center gap-2">
                    <button onclick="verEstadisticas('${correo}')" class="text-indigo-600 hover:text-indigo-900 font-bold bg-indigo-50 hover:bg-indigo-100 px-3 py-2 rounded shadow-sm">
                        <i class="fas fa-chart-line"></i>
                    </button>
                    ${botonBorrar}
                </td>
            </tr>`;
    });
}

// 3. BUSCADOR
document.getElementById('buscadorAlumnos').addEventListener('input', function(e) {
    const texto = e.target.value.toLowerCase();
    const filtrados = alumnosGlobales.filter(al =>
        (al.correo || al.correoUsuario || "").toLowerCase().includes(texto)
    );
    pintarTabla(filtrados);
});

// 4. VER ESTADÍSTICAS
window.verEstadisticas = async function(correo) {
    document.getElementById('modalCorreo').innerText = correo;
    document.getElementById('modalEstadisticas').classList.remove('hidden');
    document.getElementById('contenedorDatosModal').classList.add('hidden');
    document.getElementById('mensajeErrorModal').classList.add('hidden');

    try {
        const response = await fetch(`${API_URL}/evaluaciones/estadisticas/${correo}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });

        if (response.ok) {
            const stats = await response.json();
            document.getElementById('modalMedia').innerText = stats.notaMedia || stats.nota_media || 0;
            document.getElementById('modalTotal').innerText = stats.totalExamenes || stats.total_examenes_realizados || 0;
            document.getElementById('modalAprobados').innerText = stats.aprobados || stats.examenes_aprobados || 0;
            document.getElementById('modalSuspensos').innerText = stats.suspensos || stats.examenes_suspendidos || 0;
            document.getElementById('contenedorDatosModal').classList.remove('hidden');
        } else {
            document.getElementById('mensajeErrorModal').classList.remove('hidden');
        }
    } catch (error) {
        alert("Fallo al obtener estadísticas.");
    }
};

// 5. CERRAR MODAL
window.cerrarModalEstadisticas = function() {
    document.getElementById('modalEstadisticas').classList.add('hidden');
};

// 6. EXPORTAR EXCEL
window.exportarAlumnosAExcel = async function() {
    try {
        const response = await fetch(`${API_URL}/alumnos/exportar/excel`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error();

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `alumnos_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a);
        a.click();
        a.remove();
    } catch (error) {
        alert("Error al exportar Excel.");
    }
};

// 7. BORRAR ALUMNO
window.borrarAlumno = async function(correo) {
    if (!confirm(`¿Dar de baja a: ${correo}?`)) return;
    try {
        const response = await fetch(`${API_URL}/alumnos/${correo}`, {
            method: 'DELETE',
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (response.ok) {
            alert("Alumno eliminado.");
            cargarAlumnos();
        } else {
            alert("Error al eliminar.");
        }
    } catch (error) {
        alert("Fallo de red.");
    }
};

// Inicializar
cargarAlumnos();