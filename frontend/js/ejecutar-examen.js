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
            // CORREGIDO: La variable id no existía, era idExamen
            document.getElementById('tituloExamen').innerText = examen.titulo || `Examen #${idExamen}`;

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

    // ¡EL CHIVATO! Esto nos dirá en la consola del navegador qué está mandando Java
    console.log("JSON real de Preguntas:", preguntasExamen);

    preguntasExamen.forEach((item, index) => {
        // Por si Spring Data REST o el Mapper lo envuelve en un sub-objeto
        const p = item.pregunta ? item.pregunta : item;

        // Autodetección: Busca el dato sin importar si es camelCase, snake_case o nulo
        const idPregunta = p.id || index + 1;
        const enunciado = p.enunciado || p.texto || "⚠️ ERROR: Enunciado vacío";
        const opA = p.opcionA || p.opcion_a || "⚠️ Vacío";
        const opB = p.opcionB || p.opcion_b || "⚠️ Vacío";
        const opC = p.opcionC || p.opcion_c || "⚠️ Vacío";
        const opD = p.opcionD || p.opcion_d || "⚠️ Vacío";

        contenedor.innerHTML += `
            <div class="bg-gray-50 p-6 rounded border border-gray-200 shadow-sm" id="bloque-pregunta-${idPregunta}">
                <h3 class="font-bold text-lg text-gray-800 mb-4"><span class="text-blue-600 mr-2">${index + 1}.</span> ${enunciado}</h3>
                
                <div class="space-y-3 pl-6">
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${idPregunta}" value="A" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción A - ${opA}</span>
                    </label>
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${idPregunta}" value="B" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción B - ${opB}</span>
                    </label>
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${idPregunta}" value="C" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción C - ${opC}</span>
                    </label>
                    <label class="flex items-center space-x-3 cursor-pointer p-2 hover:bg-blue-100 rounded transition border border-transparent hover:border-blue-200">
                        <input type="radio" name="pregunta_${idPregunta}" value="D" class="form-radio h-5 w-5 text-blue-600">
                        <span class="text-gray-700 font-medium">Opción D - ${opD}</span>
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

    // Misma lógica segura para leer las preguntas que al pintarlas
    preguntasExamen.forEach((item, index) => {
        const p = item.pregunta ? item.pregunta : item;
        const idPregunta = p.id || index + 1; // Pillamos el ID real asegurado

        const opciones = document.getElementsByName(`pregunta_${idPregunta}`);
        let valorSeleccionado = null;

        for (const radio of opciones) {
            if (radio.checked) {
                valorSeleccionado = radio.value;
                break;
            }
        }

        if (!valorSeleccionado) {
            todasRespondidas = false;
            document.getElementById(`bloque-pregunta-${idPregunta}`).classList.add('border-red-500', 'bg-red-50');
        } else {
            document.getElementById(`bloque-pregunta-${idPregunta}`).classList.remove('border-red-500', 'bg-red-50');
            respuestasUsuario[idPregunta] = valorSeleccionado;
        }
    });

    if (!todasRespondidas) {
        alert("¡Cuidado! Te has dejado preguntas sin responder (marcadas en rojo).");
        return;
    }

    const payloadEvaluacion = {
        respuestas: respuestasUsuario
    };

    console.log("Enviando a Java:", payloadEvaluacion); // Para ver qué mandamos exactamente

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
            console.log("Recibido de Java:", resultado); // Para ver qué nos devuelve

            // Autodetección: Si no encuentra notaFinal, busca nota_final por si acaso
            const nota = resultado.notaFinal !== undefined ? resultado.notaFinal : resultado.nota_final;
            const blancas = resultado.enBlanco !== undefined ? resultado.enBlanco : resultado.en_blanco;

            alert(`¡Examen entregado correctamente!\n\nTu Nota: ${nota} / 10\nAciertos: ${resultado.aciertos}\nFallos: ${resultado.fallos}\nEn Blanco: ${blancas}`);
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