const token = localStorage.getItem('token');
if (!token) window.location.href = 'index.html';

const API_URL = 'http://localhost:8080';
let ultimoExamenBuscado = null; // Para saber qué Excel descargar

// 1. BUSCAR RANKING DE UN EXAMEN
window.buscarPorExamen = async function() {
    const idExamen = document.getElementById('inputExamenId').value;
    if (!idExamen) return alert("Escribe un ID de examen");

    ocultarEstadisticas();
    mostrarCarga("Buscando notas del examen #" + idExamen + "...");

    try {
        const response = await fetch(`${API_URL}/evaluaciones/examen/${idExamen}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const notas = await response.json();
            pintarTabla(notas, `Ranking del Examen #${idExamen}`);

            // Mostramos el botón de Excel y guardamos el ID
            ultimoExamenBuscado = idExamen;
            document.getElementById('btnDescargarExcel').classList.remove('hidden');
        } else if (response.status === 404) {
            pintarVacio("Nadie ha realizado este examen aún o el examen no existe.");
            document.getElementById('btnDescargarExcel').classList.add('hidden');
        } else {
            throw new Error();
        }
    } catch (error) {
        pintarVacio("Error al conectar con el servidor.");
    }
}

// 2. BUSCAR HISTORIAL Y ESTADÍSTICAS DE UN ALUMNO
window.buscarPorAlumno = async function() {
    const correo = document.getElementById('inputCorreo').value;
    if (!correo) return alert("Escribe el correo del alumno");

    document.getElementById('btnDescargarExcel').classList.add('hidden');
    mostrarCarga("Buscando expediente de " + correo + "...");

    try {
        // A. Pedimos las estadísticas
        const resStats = await fetch(`${API_URL}/evaluaciones/estadisticas/${correo}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        // B. Pedimos el historial
        const resHistorial = await fetch(`${API_URL}/evaluaciones/alumno/${correo}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (resHistorial.ok && resStats.ok) {
            const stats = await resStats.json();
            const historial = await resHistorial.json();

            mostrarEstadisticas(stats);
            pintarTabla(historial, `Expediente de: ${correo}`);
        } else if (resHistorial.status === 404) {
            ocultarEstadisticas();
            pintarVacio("Este alumno no ha realizado ningún examen.");
        } else {
            throw new Error();
        }
    } catch (error) {
        ocultarEstadisticas();
        pintarVacio("Error al conectar con el servidor.");
    }
}

// 3. DESCARGAR EL EXCEL (Truco para enviar el Token JWT)
window.descargarExcel = async function() {
    if (!ultimoExamenBuscado) return;

    try {
        const response = await fetch(`${API_URL}/evaluaciones/examen/${ultimoExamenBuscado}/exportar/excel`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            // Convertimos la respuesta binaria a un Blob
            const blob = await response.blob();
            // Creamos una URL temporal para el archivo
            const urlDescarga = window.URL.createObjectURL(blob);

            // Forzamos la descarga creando un enlace invisible y haciéndole clic
            const a = document.createElement('a');
            a.href = urlDescarga;
            a.download = `notas_examen_${ultimoExamenBuscado}.xlsx`;
            document.body.appendChild(a);
            a.click();
            a.remove();
        } else {
            alert("Error al generar el Excel.");
        }
    } catch (error) {
        alert("Fallo de conexión al descargar.");
    }
}

// --- FUNCIONES AUXILIARES DE PINTADO ---

function pintarTabla(listaNotas, titulo) {
    document.getElementById('tituloResultados').innerText = titulo;
    const tabla = document.getElementById('tablaEvaluaciones');
    tabla.innerHTML = '';

    listaNotas.forEach(nota => {
        // Coloreamos la nota de verde o rojo según si aprueba o no
        const colorNota = nota.nota >= 5.0 ? 'text-green-600' : 'text-red-600';
        const fecha = new Date(nota.fecha).toLocaleString();

        tabla.innerHTML += `
            <tr class="hover:bg-gray-50 border-b border-gray-200">
                <td class="px-5 py-3 text-sm text-gray-500">#${nota.idEvaluacion}</td>
                <td class="px-5 py-3 text-sm font-bold text-gray-800">Examen #${nota.idExamen}</td>
                <td class="px-5 py-3 text-sm text-gray-700">${nota.correoUsuario || 'N/A'}</td>
                <td class="px-5 py-3 text-sm text-center text-gray-500">${fecha}</td>
                <td class="px-5 py-3 text-sm text-center font-black ${colorNota}">${nota.nota}</td>
            </tr>
        `;
    });
}

function mostrarEstadisticas(stats) {
    document.getElementById('zonaEstadisticas').classList.remove('hidden');
    document.getElementById('statTotal').innerText = stats.total_examenes_realizados;
    document.getElementById('statMedia').innerText = stats.nota_media;
    document.getElementById('statAprobados').innerText = stats.examenes_aprobados;
    document.getElementById('statSuspensos').innerText = stats.examenes_suspendidos;
}

function ocultarEstadisticas() {
    document.getElementById('zonaEstadisticas').classList.add('hidden');
}

function mostrarCarga(mensaje) {
    document.getElementById('tituloResultados').innerText = "Buscando...";
    document.getElementById('tablaEvaluaciones').innerHTML = `<tr><td colspan="5" class="text-center py-8 text-blue-500 font-bold animate-pulse">${mensaje}</td></tr>`;
}

function pintarVacio(mensaje) {
    document.getElementById('tituloResultados').innerText = "Sin resultados";
    document.getElementById('tablaEvaluaciones').innerHTML = `<tr><td colspan="5" class="text-center py-8 text-gray-500">${mensaje}</td></tr>`;
}