const token = localStorage.getItem('token');
if (!token) window.top.location.href = 'index.html';

const API_URL = 'http://localhost:8080';

async function cargarMisNotas() {
    const tabla = document.getElementById('tablaMisNotas');

    try {
        // Atacamos a tu endpoint protegido con hasAuthority('ALUMNO')
        const response = await fetch(`${API_URL}/evaluaciones/mis-notas`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.status === 401 || response.status === 403) {
            alert("Sesión caducada o sin permisos.");
            window.top.location.href = 'index.html';
            return;
        }

        if (response.ok) {
            const notas = await response.json();

            if (!notas || notas.length === 0) {
                tabla.innerHTML = `<tr><td colspan="4" class="text-center py-10 text-gray-500 font-bold"><i class="fas fa-folder-open text-4xl mb-3 block text-gray-300"></i>Aún no has realizado ningún examen.</td></tr>`;
                // Dejamos las estadísticas a 0
                document.getElementById('statTotal').innerText = "0";
                document.getElementById('statMedia').innerText = "0.0";
                document.getElementById('statAprobados').innerText = "0";
                document.getElementById('statSuspensos').innerText = "0";
                return;
            }

            // --- 1. CÁLCULO DE ESTADÍSTICAS EN EL FRONTEND ---
            let totalExamenes = notas.length;
            let sumaNotas = 0;
            let aprobados = 0;
            let suspensos = 0;

            notas.forEach(n => {
                sumaNotas += n.nota;
                if (n.nota >= 5.0) aprobados++;
                else suspensos++;
            });

            const notaMedia = (sumaNotas / totalExamenes).toFixed(2);

            // Pintamos las estadísticas en las tarjetas
            document.getElementById('statTotal').innerText = totalExamenes;
            document.getElementById('statMedia').innerText = notaMedia;
            document.getElementById('statAprobados').innerText = aprobados;
            document.getElementById('statSuspensos').innerText = suspensos;

            // --- 2. PINTAR LA TABLA ---
            tabla.innerHTML = '';

            notas.forEach(nota => {
                const fechaFormat = new Date(nota.fecha).toLocaleString('es-ES', {
                    day: '2-digit', month: '2-digit', year: 'numeric',
                    hour: '2-digit', minute:'2-digit'
                });

                const estaAprobado = nota.nota >= 5.0;
                const colorNota = estaAprobado ? 'text-green-600' : 'text-red-600';
                const badgeEstado = estaAprobado
                    ? `<span class="bg-green-100 text-green-800 text-xs font-bold px-3 py-1 rounded-full">Aprobado</span>`
                    : `<span class="bg-red-100 text-red-800 text-xs font-bold px-3 py-1 rounded-full">Suspenso</span>`;

                // CORREGIDO: Usamos nota.idExamen (camelCase)
                const nombreExamen = nota.tituloExamen ? nota.tituloExamen : `Examen #${nota.idExamen}`;

                tabla.innerHTML += `
                    <tr class="hover:bg-blue-50 border-b border-gray-100 transition duration-150">
                        <td class="px-5 py-4 text-sm text-gray-600 font-medium">${fechaFormat}</td>
                        <td class="px-5 py-4 text-sm font-bold text-gray-800">${nombreExamen}</td>
                        <td class="px-5 py-4 text-center">${badgeEstado}</td>
                        <td class="px-5 py-4 text-center text-lg font-black ${colorNota}">${nota.nota}</td>
                        <td class="px-5 py-4 text-center">
                            <button onclick="descargarReporteBirt(${nota.idEvaluacion})" 
                             class="bg-red-600 hover:bg-red-700 text-white font-bold py-1 px-3 rounded shadow">
                            📄 Descargar PDF
                            </button>
                        </td>
                    </tr>
                `;
            });

        } else {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Error al cargar el historial.</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Fallo de conexión con el servidor.</td></tr>`;
    }
}

async function descargarReporteBirt(idEvaluacion) {
    // 1. Esto pausará el navegador si tienes el F12 abierto

    console.log("==========================================");
    console.log("[JS-DEBUG] 1. Iniciando petición de PDF...");
    console.log("[JS-DEBUG] 2. ID de la evaluación solicitada:", idEvaluacion);

    const urlDestino = `${API_URL}/reportes/${idEvaluacion}`;
    console.log("[JS-DEBUG] 3. URL exacta a la que disparamos:", urlDestino);
    console.log("[JS-DEBUG] 4. ¿Hay Token JWT?:", token ? "Sí, preparado." : "¡FALTA EL TOKEN!");

    try {
        const response = await fetch(urlDestino, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        console.log("[JS-DEBUG] --- RESPUESTA DEL SERVIDOR ---");
        console.log("[JS-DEBUG] 5. Código de estado HTTP:", response.status);
        console.log("[JS-DEBUG] 6. ¿Respuesta OK?:", response.ok);

        if (response.ok) {
            console.log("[JS-DEBUG] 7. ¡Éxito! Java ha devuelto el archivo. Ensamblando PDF...");
            const blob = await response.blob();
            console.log("[JS-DEBUG] 8. Tamaño del archivo recibido:", blob.size, "bytes");

            const urlDescarga = window.URL.createObjectURL(blob);
            const enlaceFalso = document.createElement('a');
            enlaceFalso.href = urlDescarga;
            enlaceFalso.download = `Reporte_Examen_${idEvaluacion}.pdf`;
            document.body.appendChild(enlaceFalso);
            enlaceFalso.click();
            document.body.removeChild(enlaceFalso);
            window.URL.revokeObjectURL(urlDescarga);

            console.log("[JS-DEBUG] 9. Archivo descargado en tu ordenador.");
        } else {
            console.error("[JS-DEBUG] ❌ El servidor ha rechazado la petición.");
            // Leemos qué mensaje de error exacto ha mandado Spring Boot
            const textoError = await response.text();
            console.error("[JS-DEBUG] Mensaje de Java:", textoError);
            alert(`Error del servidor: ${response.status}. Revisa la consola F12.`);
        }
    } catch (error) {
        console.error("[JS-DEBUG] ❌ FALLO DE RED (Gateway apagado o CORS):", error);
        alert("Fallo de red total. ¿Está Docker encendido?");
    }
    console.log("==========================================");
}
// Arrancamos
cargarMisNotas();