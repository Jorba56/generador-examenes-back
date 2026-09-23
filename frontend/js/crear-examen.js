const token = localStorage.getItem('token');
if (!token) window.location.href = 'index.html';

const API_URL = 'http://localhost:8080';
const formGenerarExamen = document.getElementById('formGenerarExamen');
const btnGenerar = document.getElementById('btnGenerar');

formGenerarExamen.addEventListener('submit', async (e) => {
    e.preventDefault(); // Evita que la página recargue

    // Cambiamos el texto del botón para que el usuario sepa que está cargando
    const textoOriginal = btnGenerar.innerHTML;
    btnGenerar.innerHTML = '⚙️ Generando...';
    btnGenerar.disabled = true;

    const titulo = document.getElementById('titulo').value;
    const descripcion = document.getElementById('descripcion').value;
    const numPreguntas = document.getElementById('numPreguntas').value;

    // 2. Como Java pide @RequestParam, empaquetamos los datos para la URL
    const params = new URLSearchParams({
        titulo: titulo,
        descripcion: descripcion,
        numPreguntas: numPreguntas
    });

    try {
        // 3. Pasamos los parámetros concatenados en la URL usando ?
        const response = await fetch(`${API_URL}/examenes/generar?${params.toString()}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
                // Ya no mandamos JSON, así que quitamos el Content-Type y el body
            }
        });

        if (response.ok) {
            alert("¡Examen generado con éxito!");
            window.location.href = 'dashboard.html';
        } else if (response.status === 404) {
            // Cazamos la NotFoundException que pusiste en tu Java
            alert("No hay suficientes preguntas en la base de datos para generar este examen.");
        } else {
            const errorTxt = await response.text();
            alert("Error del servidor: " + errorTxt);
        }
    } catch (error) {
        console.error("Fallo de conexión:", error);
        alert("Fallo de conexión con el servidor.");
    } finally {
        // Restauramos el botón por si hubo un error y el usuario quiere reintentar
        btnGenerar.innerHTML = textoOriginal;
        btnGenerar.disabled = false;
    }
});