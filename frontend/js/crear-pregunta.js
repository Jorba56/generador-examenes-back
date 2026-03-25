const token = localStorage.getItem('token');
if (!token) window.location.href = 'index.html';

const API_URL = 'http://localhost:8080';
const urlParams = new URLSearchParams(window.location.search);
const idPregunta = urlParams.get('id'); // Si hay ID, estamos editando

// Si es edición, cargamos los datos previos
if (idPregunta) {
    document.getElementById('tituloPagina').textContent = "Editar Pregunta #" + idPregunta;
    cargarDatosPrevios();
}

async function cargarDatosPrevios() {
    const res = await fetch(`${API_URL}/preguntas/${idPregunta}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (res.ok) {
        const p = await res.json();
        document.getElementById('enunciado').value = p.enunciado;
        document.getElementById('opcionA').value = p.opcion_a || p.opcion_a;
        document.getElementById('opcionB').value = p.opcion_b || p.opcion_b;
        document.getElementById('opcionC').value = p.opcion_c || p.opcion_c;
        document.getElementById('opcionD').value = p.opcion_d || p.opcion_d || '';
        document.getElementById('correcta').value = p.correcta;
    }
}

document.getElementById('formPregunta').addEventListener('submit', async (e) => {
    e.preventDefault();

    // Recogemos los valores del HTML
    const valEnunciado = document.getElementById('enunciado').value;
    const valOpcionA = document.getElementById('opcionA').value;
    const valOpcionB = document.getElementById('opcionB').value;
    const valOpcionC = document.getElementById('opcionC').value;
    const valOpcionD = document.getElementById('opcionD').value;
    const valCorrecta = document.getElementById('correcta').value;

    // BLINDAJE: Enviamos las opciones tanto en minúsculas como en snake_case
    // para que Spring Boot (Jackson) lo pille sí o sí, sin importar cómo se llame en tu entidad Java.
    const payload = {
        enunciado: valEnunciado,
        correcta: valCorrecta,

        // Formato camelCase / minúsculas
        opcionA: valOpcionA, opciona: valOpcionA,
        opcionB: valOpcionB, opcionb: valOpcionB,
        opcionC: valOpcionC, opcionc: valOpcionC,
        opcionD: valOpcionD, opciond: valOpcionD,

        // Formato snake_case
        opcion_a: valOpcionA,
        opcion_b: valOpcionB,
        opcion_c: valOpcionC,
        opcion_d: valOpcionD
    };

    // Si hay ID, hacemos PUT. Si no, hacemos POST.
    const url = idPregunta ? `${API_URL}/preguntas/${idPregunta}` : `${API_URL}/preguntas`;
    const metodo = idPregunta ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: metodo,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert(idPregunta ? "¡Pregunta actualizada!" : "¡Pregunta creada!");
            window.location.href = 'preguntas.html'; // Volvemos a la tabla
        } else {
            // MEJORA: Si falla, extraemos el texto del error de Spring Boot para no tener que adivinar
            const errorTxt = await response.text();
            alert("Error al guardar la pregunta: \n" + errorTxt);
        }
    } catch (error) {
        alert("Fallo de conexión al intentar guardar.");
    }
});