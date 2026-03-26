// Asegúrate de que esta URL apunta a tu API Gateway
const API_URL = 'http://localhost:8080';

document.addEventListener("DOMContentLoaded", () => {
    const formRegistro = document.getElementById('formRegistro');
    const mensajeError = document.getElementById('mensajeError');
    const textoError = document.getElementById('textoError');
    const btnRegistrar = document.getElementById('btnRegistrar');

    formRegistro.addEventListener('submit', async (e) => {
        e.preventDefault(); // Evitamos que la página se recargue

        // 1. Recogemos los valores del formulario
        const nombre = document.getElementById('regNombre').value.trim();
        const apellidos = document.getElementById('regApellidos').value.trim();
        const correo = document.getElementById('regCorreo').value.trim();
        const password = document.getElementById('regPassword').value;
        const passwordConfirm = document.getElementById('regPasswordConfirm').value;

        // 2. Validación Frontend básica
        if (password !== passwordConfirm) {
            mostrarError("Las contraseñas no coinciden. Por favor, revísalas.");
            return;
        }

        if (password.length < 4) {
            mostrarError("La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        // 3. Preparamos el Payload adaptado a tu UserAddDTO
        const payload = {
            nombre_usuario: nombre,
            apellido_usuario: apellidos,
            correo_usuario: correo,
            contrasenha_usuario: password
        };

        // 4. Cambiamos el estado del botón para que sepa que está cargando
        btnRegistrar.disabled = true;
        btnRegistrar.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Registrando...';
        mensajeError.classList.add('hidden');

        // 5. Atacamos al backend
        try {
            const response = await fetch(`${API_URL}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                // ¡Éxito!
                alert("¡Registro completado con éxito! Ahora puedes iniciar sesión.");
                // Redirigimos al login
                window.location.href = 'login.html';
            } else {
                // Si el backend da error (ej. el correo ya existe)
                const errorText = await response.text();
                try {
                    const errorJson = JSON.parse(errorText);
                    mostrarError(errorJson.message || errorJson.error || "El correo ya está registrado o hay datos inválidos.");
                } catch (e) {
                    mostrarError(`Error del servidor (${response.status}): ${errorText || 'Inténtalo más tarde.'}`);
                }
            }
        } catch (error) {
            mostrarError("Fallo de red al intentar conectar con el servidor. Revisa si el backend está encendido.");
            console.error(error);
        } finally {
            // Restauramos el botón
            btnRegistrar.disabled = false;
            btnRegistrar.innerHTML = '<span>Registrarse</span> <i class="fas fa-arrow-right ml-2"></i>';
        }
    });

    // Función auxiliar para mostrar errores chulos en la UI
    function mostrarError(mensaje) {
        textoError.innerText = mensaje;
        mensajeError.classList.remove('hidden');
    }
});