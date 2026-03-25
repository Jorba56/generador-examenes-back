const token = localStorage.getItem('token');
if (!token) window.top.location.href = 'index.html';

const API_URL = 'http://localhost:8080';

// Sacamos el ID del examen de la URL
const params = new URLSearchParams(window.location.search);
const idExamen = params.get('id');

if (!idExamen) {
    alert("No se ha especificado ningún examen.");
    window.location.href = 'examenes-disponibles.html';
}

let preguntasExamen = [];

// --- 1. CARGAR EXAMEN Y PREGUNTAS ---
async function iniciarExamen() {
    try {
        // Tu endpoint GET /examenes/{id} devuelve el ExamenDetalleDTO con las preguntas dentro
        const response = await fetch(`${API_URL}/examenes/${idExamen}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.status === 403) {
            alert("No tienes permiso para ver este examen.");
            window.location.href = 'examenes-disponibles.html';
            return;
        }

        if (response.ok) {
            const examen = await response.json();
            document.getElementById('tituloExamen').innerText = examen.titulo || `Examen #${id}`;

            // Extraemos las preguntas del DTO (Asegúrate de que la variable se llame 'preguntas' en tu DTO)
            preguntasExamen = examen.preguntas || [];
            pintarPreguntas();
        } else {
            document.getElementById('contenedorPreguntas').innerHTML = `<p class="text-red-500 font-bold text-center">Error al cargar el examen.</p>`;
        }
    } catch (error) {
        document.getElementById('contenedorPreguntas').innerHTML = `<p class="text-red-500 font-bold text-center">Fallo de conexión.</p>`;
    }
}

// --- 2. DIBUJAR LAS PREGUNTAS EN EL HTML ---
function pintarPreguntas() {
    const contenedor = document.getElementById('contenedorPreguntas');
    contenedor.innerHTML = '';

    if (preguntasExamen.length === 0) {
        contenedor.innerHTML = `<p class="text-gray-500 text-center font-bold">Este examen aún no tiene preguntas asignadas.</p>`;
        return;
    }

    preguntasExamen.forEach((p, index) => {
        contenedor.innerHTML += `
            <div class="bg-gray-50 p-6 rounded border border-gray-200 shadow-sm" id="bloque-pregunta-${p.id}">
                <h3 class="font-bold text-lg text-gray-800 mb-4"><span class="text-blue-600 mr-2">${index + 1}.</span> ${p.enunciado}</h3>
                
                <div class="space-y-3 pl-6">
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${p.id}" value="A" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción A ${p.opcion_a ? '- ' + p.opcion_a : ''}</span>
                    </label>
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${p.id}" value="B" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción B ${p.opcion_b ? '- ' + p.opcion_b : ''}</span>
                    </label>
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${p.id}" value="C" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción C ${p.opcion_c ? '- ' + p.opcion_c : ''}</span>
                    </label>
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${p.id}" value="D" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción D ${p.opcion_d ? '- ' + p.opcion_d : ''}</span>
                    </label>
                </div>
            </div>
        `;
    });

    document.getElementById('zonaEntrega').classList.remove('hidden');
}

// --- 3. RECOGER RESPUESTAS Y ENVIAR AL BACKEND ---
async function entregarExamen() {
    if (!confirm("¿Estás seguro de que quieres entregar el examen ya? Revisa tus respuestas.")) return;

    let respuestasUsuario = {};
    let todasRespondidas = true;

    // Fíjate que ahora usamos (p, index) para saber si es la pregunta 1, 2, 3...
    preguntasExamen.forEach((p, index) => {
        const opciones = document.getElementsByName(`pregunta_${p.id}`);
        let valorSeleccionado = null;

        for (const radio of opciones) {
            if (radio.checked) {
                valorSeleccionado = radio.value;
                break;
            }
        }

        if (!valorSeleccionado) {
            todasRespondidas = false;
            document.getElementById(`bloque-pregunta-${p.id}`).classList.add('border-red-500', 'bg-red-50');
        } else {
            document.getElementById(`bloque-pregunta-${p.id}`).classList.remove('border-red-500', 'bg-red-50');

            // AQUÍ ESTÁ LA MAGIA: Tu backend espera 1, 2, 3...
            // Como los arrays en JS empiezan en 0, le sumamos 1.
            const numeroPreguntaParaJava = index + 1;
            respuestasUsuario[numeroPreguntaParaJava] = valorSeleccionado;
        }
    });

    if (!todasRespondidas) {
        alert("¡Cuidado! Te has dejado preguntas sin responder (marcadas en rojo).");
        return;
    }

    const payloadEvaluacion = {
        respuestas: respuestasUsuario
    };

    try {
        const response = await fetch(`${API_URL}/evaluaciones/${idExamen}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payloadEvaluacion)
        });

        if (response.ok) {
            const resultado = await response.json();
            alert(`¡Examen entregado correctamente!\n\nTu Nota: ${resultado.nota_final} / 10\nAciertos: ${resultado.aciertos}\nFallos: ${resultado.fallos}\nEn Blanco: ${resultado.en_blanco}`);
            window.location.href = 'mis-notas.html';
        } else if (response.status === 400) {
            alert("El servidor ha rechazado la entrega. Has agotado tus intentos permitidos.");
            window.location.href = 'examenes-disponibles.html';
        } else {
            alert("Fallo interno del servidor.");
        }
    } catch (error) {
        alert("Fallo de red al enviar el examen.");
    }
}


    function abandonarExamen() {
        if (confirm("Si abandonas ahora, perderás el progreso. ¿Salir?")) {
            window.location.href = 'examenes-disponibles.html';
        }
    }

iniciarExamen();