const token = localStorage.getItem('token');
if (!token) {
    window.location.href = 'index.html';
}

const API_URL = 'http://localhost:8080';

// Extraemos el ID de la URL (ej: examen-detalle.html?id=5)
const urlParams = new URLSearchParams(window.location.search);
const idExamen = urlParams.get('id');

if (!idExamen) {
    alert("No se ha especificado ningún examen.");
    window.location.href = 'dashboard.html';
}

async function cargarDetallesExamen() {
    try {
        const response = await fetch(`${API_URL}/examenes/${idExamen}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const examen = await response.json();

            // Pintamos la cabecera
            document.getElementById('tituloExamen').textContent = examen.titulo || 'Sin Título';
            document.getElementById('descExamen').textContent = examen.descripcion || 'Sin descripción';

            const contenedor = document.getElementById('contenedorPreguntas');
            contenedor.innerHTML = ''; // Limpiamos el texto de carga

            if (!examen.preguntas || examen.preguntas.length === 0) {
                contenedor.innerHTML = '<p class="text-gray-500 text-center">Este examen aún no tiene preguntas.</p>';
                return;
            }

            // Pintamos cada pregunta con sus opciones
            examen.preguntas.forEach((pregunta, index) => {
                const tarjetaHtml = `
                    <div class="bg-white p-5 rounded-lg shadow border border-gray-200">
                        <h4 class="font-bold text-lg text-gray-800 mb-3">
                            <span class="text-blue-600 mr-2">${index + 1}.</span>${pregunta.enunciado}
                        </h4>
                        <div class="pl-6 space-y-2">
                            <p class="text-gray-700 p-2 bg-gray-50 rounded border">A) ${pregunta.opcionA}</p>
                            <p class="text-gray-700 p-2 bg-gray-50 rounded border">B) ${pregunta.opcionB}</p>
                            <p class="text-gray-700 p-2 bg-gray-50 rounded border">C) ${pregunta.opcionC}</p>
                            <p class="text-gray-700 p-2 bg-gray-50 rounded border">D) ${pregunta.opcionD}</p>
                        </div>
                    </div>
                `;
                contenedor.innerHTML += tarjetaHtml;
            });

        } else if (response.status === 404) {
            alert("El examen no existe.");
            window.location.href = 'dashboard.html';
        } else {
            throw new Error("No tienes permisos o el servidor falló.");
        }
    } catch (error) {
        console.error("Error:", error);
        document.getElementById('tituloExamen').textContent = "Error de conexión";
        document.getElementById('descExamen').textContent = "No se pudieron cargar los datos del servidor.";
        document.getElementById('contenedorPreguntas').innerHTML = '';
    }
}

// Iniciamos la carga
cargarDetallesExamen();