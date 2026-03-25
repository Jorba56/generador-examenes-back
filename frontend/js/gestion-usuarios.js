const token = localStorage.getItem('token');
if (!token) window.top.location.href = 'index.html';

const API_URL = 'http://localhost:8080';

let pagUsuarios = 0;

// --- CONTROL DE PESTAÑAS ---
window.cambiarPestana = function(pestana) {
    const secUsu = document.getElementById('seccion-usuarios');
    const secRol = document.getElementById('seccion-roles');
    const tabUsu = document.getElementById('tab-usuarios');
    const tabRol = document.getElementById('tab-roles');

    if (pestana === 'usuarios') {
        secUsu.classList.remove('hidden');
        secRol.classList.add('hidden');

        tabUsu.classList.add('bg-blue-100', 'text-blue-700');
        tabUsu.classList.remove('text-gray-600', 'hover:bg-gray-100');
        tabRol.classList.remove('bg-purple-100', 'text-purple-700');
        tabRol.classList.add('text-gray-600', 'hover:bg-gray-100');

        cargarUsuarios();
    } else {
        secUsu.classList.add('hidden');
        secRol.classList.remove('hidden');

        tabRol.classList.add('bg-purple-100', 'text-purple-700');
        tabRol.classList.remove('text-gray-600', 'hover:bg-gray-100');
        tabUsu.classList.remove('bg-blue-100', 'text-blue-700');
        tabUsu.classList.add('text-gray-600', 'hover:bg-gray-100');

        cargarRoles();
    }
}

// ==================== LÓGICA DE USUARIOS ====================

window.cambiarPaginaUsu = function(dir) {
    pagUsuarios += dir;
    cargarUsuarios();
}

// ==================== LÓGICA DE USUARIOS ====================

window.cambiarPaginaUsu = function(dir) {
    pagUsuarios += dir;
    cargarUsuarios();
}

async function cargarUsuarios() {
    const tabla = document.getElementById('tablaUsuarios');
    tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-blue-500 animate-pulse font-bold">Cargando usuarios y roles...</td></tr>`;

    try {
        // Lanzamos las 3 peticiones SIMULTÁNEAMENTE para no perder rendimiento
        const [resUsu, resRoles, resMapa] = await Promise.all([
            fetch(`${API_URL}/usuarios/paginados?page=${pagUsuarios}&size=10&sortBy=idUser&sortDir=asc`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_URL}/roles`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_URL}/usuarios_roles`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (resUsu.ok && resRoles.ok && resMapa.ok) {
            const dataUsu = await resUsu.json();
            const todosLosRoles = await resRoles.json();
            const mapaUsuariosRoles = await resMapa.json(); // Esto trae tu List<UsuarioRolesDTO>

            const usuarios = dataUsu.content || [];

            // 1. Creamos un diccionario rápido de Roles para no hacer bucles anidados
            // Ejemplo: { 1: "ADMIN", 2: "PROFESOR" }
            const diccRoles = {};
            todosLosRoles.forEach(r => {
                const idRol = r.idRol || r.id;
                diccRoles[idRol] = r.name || r.nombre || 'Desconocido';
            });

            // 2. Controles de Paginación
            document.getElementById('infoPagUsuarios').innerText = `Página ${dataUsu.number + 1} de ${dataUsu.totalPages || 1}`;
            document.getElementById('btnAntUsu').disabled = dataUsu.number === 0;
            document.getElementById('btnSigUsu').disabled = dataUsu.number >= (dataUsu.totalPages - 1);

            tabla.innerHTML = '';

            if (usuarios.length === 0) {
                tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-gray-500 font-bold">No hay usuarios registrados.</td></tr>`;
                return;
            }

            // 3. Pintamos la tabla cruzando los datos
            usuarios.forEach(u => {
                // Adaptado al snake_case o camelCase de tu UsersAllDTO
                const idUser = u.id_usuario || u.idUser;
                const nombreCompleto = `${u.nombre_usuario || u.nombreUsuario || ''} ${u.apellido_usuario || u.apellidoUsuario || ''}`.trim();
                const correo = u.correo_usuario || u.emailUsuario || 'Sin correo';

                // Buscamos este usuario en la relación que devolvió el endpoint /usuarios_roles
                const relacion = mapaUsuariosRoles.find(m => (m.id_usuario || m.idUser) === idUser);

                let rolesHtml = '<span class="text-gray-400 text-xs italic">Sin roles</span>';

                // Si encontramos la relación y tiene IDs de roles, los pintamos
                if (relacion && relacion.id_rol && relacion.id_rol.length > 0) {
                    rolesHtml = relacion.id_rol.map(idRol => {
                        // Rescatamos el nombre del rol usando nuestro diccionario
                        const nombreRol = diccRoles[idRol] || `ID:${idRol}`;
                        return `<span class="bg-blue-100 text-blue-800 text-xs font-bold px-2 py-1 rounded border border-blue-200 mr-1 shadow-sm uppercase">${nombreRol}</span>`;
                    }).join('');
                }

                tabla.innerHTML += `
                    <tr class="hover:bg-blue-50 border-b border-gray-100 transition">
                        <td class="px-5 py-3 text-sm font-bold text-gray-800">${nombreCompleto || 'Usuario N/A'}</td>
                        <td class="px-5 py-3 text-sm text-gray-600">${correo}</td>
                        <td class="px-5 py-3 text-center">${rolesHtml}</td>
                        <td class="px-5 py-3 text-center">
                            <button onclick="editarUsuario(${idUser})" class="text-blue-600 hover:text-blue-900 font-bold mr-3" title="Editar Usuario"><i class="fas fa-edit"></i></button>
                            <button onclick="borrarUsuario(${idUser})" class="text-red-600 hover:text-red-900 font-bold" title="Eliminar Usuario"><i class="fas fa-trash"></i></button>
                        </td>
                    </tr>
                `;
            });
        } else {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Error de permisos o conexión con uno de los endpoints.</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Fallo de red al conectar.</td></tr>`;
    }
}
window.exportarExcelUsuarios = function() {
    window.open(`${API_URL}/usuarios/exportar/excel`, '_blank');
}

window.editarUsuario = async function(id) {
    try {
        // Pedimos a tu endpoint GET /usuarios/{id} los datos frescos
        const response = await fetch(`${API_URL}/usuarios/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const usuario = await response.json();

            // Rellenamos los campos del formulario respetando tu DTO
            document.getElementById('editUserId').value = id;
            document.getElementById('editNombre').value = usuario.nombre_usuario || usuario.nombreUsuario || '';
            document.getElementById('editApellidos').value = usuario.apellido_usuario || usuario.apellidoUsuario || '';
            document.getElementById('editCorreo').value = usuario.correo_usuario || usuario.emailUsuario || '';

            // Si por algún casual la base de datos lo tiene en false, quitamos el check
            document.getElementById('editActivo').checked = usuario.activo !== false;

            // Mostramos la ventana
            document.getElementById('modalEditarUsuario').classList.remove('hidden');
        } else {
            alert("No se pudieron cargar los datos del usuario.");
        }
    } catch (error) {
        alert("Fallo de red al intentar conectar.");
    }
}

// --- CERRAR MODAL ---
window.cerrarModalEditar = function() {
    document.getElementById('modalEditarUsuario').classList.add('hidden');
}

// --- GUARDAR LOS CAMBIOS (PUT) ---
window.guardarEdicionUsuario = async function() {
    const id = document.getElementById('editUserId').value;

    // Construimos el JSON exactamente como espera tu Entidad User.java
    // No enviamos contraseña ni roles porque tu backend lo prohíbe/ignora para el ADMIN
    const payload = {
        nombre_usuario: document.getElementById('editNombre').value,
        apellido_usuario: document.getElementById('editApellidos').value,
        correo_usuario: document.getElementById('editCorreo').value,
        activo: document.getElementById('editActivo').checked
    };

    try {
        // Atacamos a tu endpoint PUT /usuarios/{id}
        const response = await fetch(`${API_URL}/usuarios/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("¡Usuario modificado con éxito!");
            cerrarModalEditar();
            cargarUsuarios(); // Recargamos la tabla automáticamente para ver los cambios
        } else {
            // El detector de mentiras por si falla algo en el backend
            const errorText = await response.text();
            try {
                const errorJson = JSON.parse(errorText);
                alert(`Error: ${errorJson.message || errorJson.error || 'Desconocido'}`);
            } catch (e) {
                alert(`Error del Servidor (${response.status}).`);
            }
        }
    } catch (error) {
        alert("Fallo de conexión al guardar.");
    }
}
window.borrarUsuario = function(id) { alert("Llamar al DELETE /usuarios/" + id); }

// ==================== LÓGICA DE ROLES ====================

async function cargarRoles() {
    const tabla = document.getElementById('tablaRoles');
    tabla.innerHTML = `<tr><td colspan="3" class="text-center py-8 text-purple-500 animate-pulse font-bold">Cargando roles...</td></tr>`;

    try {
        const response = await fetch(`${API_URL}/roles`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const roles = await response.json();
            tabla.innerHTML = '';

            if (roles.length === 0) {
                tabla.innerHTML = `<tr><td colspan="3" class="text-center py-8 text-gray-500 font-bold">No hay roles creados.</td></tr>`;
                return;
            }

            roles.forEach(r => {
                tabla.innerHTML += `
                    <tr class="hover:bg-purple-50 border-b border-gray-100 transition">
                        <td class="px-5 py-3 text-sm font-bold text-gray-500">#${r.id_rol || r.id}</td>
                        <td class="px-5 py-3 text-sm font-bold text-purple-700">${r.name || r.nombre}</td>
                        <td class="px-5 py-3 text-center">
                            <button onclick="editarRol(${r.idRol || r.id})" class="text-blue-600 hover:text-blue-900 font-bold mr-3"><i class="fas fa-edit"></i></button>
                            <button onclick="borrarRol(${r.idRol || r.id})" class="text-red-600 hover:text-red-900 font-bold"><i class="fas fa-trash"></i></button>
                        </td>
                    </tr>
                `;
            });
        } else {
            tabla.innerHTML = `<tr><td colspan="3" class="text-center py-8 text-red-500">Error al cargar roles.</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="3" class="text-center py-8 text-red-500 font-bold">Fallo de red al conectar.</td></tr>`;
    }
}

window.crearRol = function() { alert("Abrir modal para crear rol llamando a POST /roles"); }
window.editarRol = function(id) { alert("Abrir modal para editar rol " + id + " llamando a PUT /roles/" + id); }
window.borrarRol = function(id) { alert("Llamar a DELETE /roles/" + id + " (Borrado lógico)"); }

// Arrancamos en la pestaña por defecto
cargarUsuarios();