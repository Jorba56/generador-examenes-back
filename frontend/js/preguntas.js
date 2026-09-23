const token = localStorage.getItem('token');
async function cogerIdUser() {
    let idUsuario;
    try {
        const responseBusqueda = await fetch(`${API_URL}/usuarios/email/${miCorreo}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });

        if (responseBusqueda.ok) {
            const usuario = await responseBusqueda.json();

            // 3. ¡LA MAGIA! A raíz del usuario encontrado, atrapamos su ID
            return idUsuario = usuario.id_usuario;
        }
    } catch {
         return ("No se ha podido conseguir tu id de usuario");
    }
}
idUsuario=cogerIdUser();
if (!token) window.location.href = 'index.html';

const API_URL = 'http://localhost:8080';

let pagPreguntas = 0;

window.cambiarPaginaPreguntas = function(direccion) {
    pagPreguntas += direccion;
    cargarPreguntas();
}

async function cargarPreguntas() {
    const tabla = document.getElementById('tablaPreguntas');
    tabla.innerHTML = `<tr><td colspan="4" class="text-center py-5">Cargando preguntas...</td></tr>`;

    try {
        const response = await fetch(`${API_URL}/preguntas/paginadas?page=${pagPreguntas}&size=10&sortBy=id&sortDir=desc`, {
            headers: {'Authorization': `Bearer ${token}`}
        });

        if (response.ok) {
            const data = await response.json();
            const preguntas = data.content || [];

            const meta = data.page || data;
            const numPag = meta.number !== undefined ? meta.number : 0;
            const totalPag = meta.totalPages || 1;

            const infoPaginacion = document.getElementById('infoPaginacionPreg');
            if (infoPaginacion) infoPaginacion.innerText = `Página ${numPag + 1} de ${totalPag}`;

            const btnAnt = document.getElementById('btnAntPreg');
            const btnSig = document.getElementById('btnSigPreg');
            if (btnAnt) btnAnt.disabled = (numPag === 0);
            if (btnSig) btnSig.disabled = (numPag >= totalPag - 1);

            tabla.innerHTML = '';

            if (preguntas.length === 0) {
                tabla.innerHTML = `<tr><td colspan="4" class="text-center py-5">El banco de preguntas está vacío.</td></tr>`;
                return;
            }

            preguntas.forEach(p => {
                tabla.innerHTML += `
                    <tr class="hover:bg-gray-50 border-b border-gray-200">
                        <td class="px-5 py-3 text-sm font-bold text-gray-500">#${p.id}</td>
                        <td class="px-5 py-3 text-sm text-gray-900">${p.enunciado}</td>
                        <td class="px-5 py-3 text-sm text-center"><span class="bg-green-100 text-green-800 font-bold py-1 px-3 rounded">${p.correcta}</span></td>
                        <td class="px-5 py-3 text-sm text-center">
                            <button onclick="editarPregunta(${p.id})" class="text-blue-600 font-bold mr-3">Editar</button>
                            <button onclick="borrarPregunta(${p.id})" class="text-red-600 font-bold">Borrar</button>
                        </td>
                    </tr>
                `;
            });
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-5 text-red-500">Error cargando preguntas</td></tr>`;
    }
}

// --- BÚSQUEDA POR ID ---
window.buscarPreguntaPorId = async function() {
    const idBuscar = document.getElementById('inputBuscarId').value.trim();

    // Si le da a buscar pero el campo está vacío, recargamos la tabla normal
    if (!idBuscar) {
        return limpiarBusquedaPreguntas();
    }

    const tabla = document.getElementById('tablaPreguntas');
    tabla.innerHTML = `<tr><td colspan="4" class="text-center py-5 text-blue-500 animate-pulse">Buscando pregunta #${idBuscar}...</td></tr>`;

    try {
        // Atacamos al endpoint de ID específico de tu PreguntaController
        const response = await fetch(`${API_URL}/preguntas/${idBuscar}`, {
            headers: { 'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const p = await response.json();

            // Adaptamos la paginación porque ahora solo hay 1 resultado
            const infoPaginacion = document.getElementById('infoPaginacionPreg');
            if(infoPaginacion) infoPaginacion.innerText = `Resultado de la búsqueda`;

            const btnAnt = document.getElementById('btnAntPreg');
            const btnSig = document.getElementById('btnSigPreg');
            if(btnAnt) btnAnt.disabled = true;
            if(btnSig) btnSig.disabled = true;

            // Pintamos la única pregunta encontrada
            tabla.innerHTML = `
                <tr class="hover:bg-blue-50 border-b border-blue-200 bg-blue-50 transition">
                    <td class="px-5 py-3 text-sm font-bold text-blue-600">#${p.id}</td>
                    <td class="px-5 py-3 text-sm text-gray-900 font-semibold">${p.enunciado}</td>
                    <td class="px-5 py-3 text-sm text-center"><span class="bg-green-100 text-green-800 font-bold py-1 px-3 rounded">${p.correcta}</span></td>
                    <td class="px-5 py-3 text-sm text-center">
                        <button onclick="editarPregunta(${p.id})" class="text-blue-600 font-bold mr-3 hover:underline">Editar</button>
                        <button onclick="borrarPregunta(${p.id})" class="text-red-600 font-bold hover:underline">Borrar</button>
                    </td>
                </tr>
            `;
        } else if (response.status === 404) {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-5 text-gray-500 font-bold"><i class="fas fa-search-minus mr-2"></i>No existe ninguna pregunta con el ID #${idBuscar}.</td></tr>`;
        } else {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-5 text-red-500">Error en la búsqueda (Status: ${response.status})</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-5 text-red-500 font-bold">Fallo de red al buscar.</td></tr>`;
    }
}

// Función para resetear el buscador y volver a la página 1 normal
window.limpiarBusquedaPreguntas = function() {
    document.getElementById('inputBuscarId').value = '';
    pagPreguntas = 0; // Volvemos a la primera página
    cargarPreguntas(); // Cargamos la tabla paginada estándar
}

window.borrarPregunta = async function (id) {
    if (!confirm(`¿Borrar definitivamente la pregunta #${id}?`)) return;

    try {
        const response = await fetch(`${API_URL}/preguntas/${id}`, {
            method: 'DELETE',
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (response.ok) cargarPreguntas();
        else alert("Error al borrar. ¿Quizás está siendo usada en un examen?");
    } catch (error) {
        alert("Fallo de red al borrar.");
    }
}

window.editarPregunta = function (id) {
    // Redirigimos al formulario pasándole el ID por la URL para que sepa que es edición
    window.location.href = `crear-pregunta.html?id=${id}`;
}

// Arrancamos la carga inicial
cargarPreguntas();