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

                // Usamos el idExamen (o el titulo si tu DTO lo incluye)
                const nombreExamen = nota.tituloExamen ? nota.tituloExamen : `Examen #${nota.id_examen}`;

                tabla.innerHTML += `
                    <tr class="hover:bg-blue-50 border-b border-gray-100 transition duration-150">
                        <td class="px-5 py-4 text-sm text-gray-600 font-medium">${fechaFormat}</td>
                        <td class="px-5 py-4 text-sm font-bold text-gray-800">${nombreExamen}</td>
                        <td class="px-5 py-4 text-center">${badgeEstado}</td>
                        <td class="px-5 py-4 text-center text-lg font-black ${colorNota}">${nota.nota}</td>
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

// Arrancamos
cargarMisNotas();