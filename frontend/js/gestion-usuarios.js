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
                const idUser = u.id_usuario || u.id_user;
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
                            <button onclick="abrirGestionRoles(${idUser})" class="text-purple-600 hover:text-purple-900 font-bold mr-3" title="Gestionar Roles"><i class="fas fa-user-tag"></i></button>
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

// Búsqueda por ID
window.buscarUsuarioPorId = async function() {
    // Asume que tienes un input en HTML con id="inputBusquedaId"
    const idABuscar = document.getElementById('inputBusquedaId').value.trim();

    if (!idABuscar) {
        pagUsuarios = 0;
        return cargarUsuarios(); // Si está vacío, recarga la tabla normal
    }

    const tabla = document.getElementById('tablaUsuarios');
    tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-blue-500 animate-pulse font-bold">Buscando usuario...</td></tr>`;

    try {
        const response = await fetch(`${API_URL}/usuarios/${idABuscar}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const usuario = await response.json();
            // Limpiamos los botones de paginación porque es un resultado único
            document.getElementById('infoPagUsuarios').innerText = `Resultado único`;
            document.getElementById('btnAntUsu').disabled = true;
            document.getElementById('btnSigUsu').disabled = true;

            renderizarUnUsuario(usuario, tabla); // Función auxiliar abajo
        } else {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Usuario no encontrado.</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Fallo de red al buscar.</td></tr>`;
    }
}

// Búsqueda por Rol
window.buscarUsuarioPorRol = async function() {
    // Asume que tienes un <select> en HTML con id="selectBusquedaRol"
    const idRolABuscar = document.getElementById('selectBusquedaRol').value;

    if (!idRolABuscar) {
        pagUsuarios = 0;
        return cargarUsuarios();
    }

    const tabla = document.getElementById('tablaUsuarios');
    tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-purple-500 animate-pulse font-bold">Filtrando por rol...</td></tr>`;

    try {
        // Asegúrate de que este endpoint exista en tu backend
        const response = await fetch(`${API_URL}/usuarios/rol/${idRolABuscar}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const usuarios = await response.json();
            document.getElementById('infoPagUsuarios').innerText = `Filtro por Rol`;
            document.getElementById('btnAntUsu').disabled = true;
            document.getElementById('btnSigUsu').disabled = true;

            tabla.innerHTML = '';
            if(usuarios.length === 0) {
                tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-gray-500 font-bold">Ningún usuario tiene este rol.</td></tr>`;
                return;
            }
            // Reutilizamos tu lógica pero iterando la lista filtrada
            usuarios.forEach(u => renderizarUnUsuario(u, tabla));
        } else {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500">Error al filtrar por rol.</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500">Fallo de red al buscar.</td></tr>`;
    }
}

// Función auxiliar para no repetir código HTML al pintar un usuario buscado
function renderizarUnUsuario(u, tabla) {
    const idUser = u.id_usuario || u.idUser;
    const nombreCompleto = `${u.nombre_usuario || u.nombreUsuario || ''} ${u.apellido_usuario || u.apellidoUsuario || ''}`.trim();
    const correo = u.correo_usuario || u.emailUsuario || 'Sin correo';

    // Como es búsqueda directa, pintamos botones genéricos para gestionar roles
    tabla.innerHTML += `
        <tr class="hover:bg-blue-50 border-b border-gray-100 transition">
            <td class="px-5 py-3 text-sm font-bold text-gray-800">${nombreCompleto}</td>
            <td class="px-5 py-3 text-sm text-gray-600">${correo}</td>
            <td class="px-5 py-3 text-center text-gray-400 text-xs italic">Ver en Gestión Roles</td>
            <td class="px-5 py-3 text-center">
                <button onclick="editarUsuario(${idUser})" class="text-blue-600 hover:text-blue-900 font-bold mr-3"><i class="fas fa-edit"></i></button>
                <button onclick="abrirGestionRoles(${idUser})" class="text-purple-600 hover:text-purple-900 font-bold mr-3" title="Gestionar Roles"><i class="fas fa-user-tag"></i></button>
                <button onclick="borrarUsuario(${idUser})" class="text-red-600 hover:text-red-900 font-bold"><i class="fas fa-trash"></i></button>
            </td>
        </tr>
    `;
}

// ==================== BÚSQUEDA DE USUARIOS ====================

// 1. Buscar por ID
window.buscarUsuarioPorId = function() {
    // Asegúrate de tener un input <input id="inputBusquedaId" type="number">
    const id = document.getElementById('inputBusquedaId').value.trim();
    if (!id) {
        pagUsuarios = 0;
        return cargarUsuarios(); // Si el input está vacío, recargamos la tabla normal
    }
    realizarBusqueda(`${API_URL}/usuarios/${id}`, true);
};

// 2. Buscar por Correo
window.buscarUsuarioPorCorreo = function() {
    // Asegúrate de tener un input <input id="inputBusquedaCorreo" type="text">
    const correo = document.getElementById('inputBusquedaCorreo').value.trim();
    if (!correo) {
        pagUsuarios = 0;
        return cargarUsuarios();
    }
    realizarBusqueda(`${API_URL}/usuarios/email/${correo}`, true);
};

// 3. Buscar por Rol
window.buscarUsuarioPorRol = function() {
    // Asegúrate de tener un <select id="selectBusquedaRol">
    const idRol = document.getElementById('selectBusquedaRol').value;
    if (!idRol) {
        pagUsuarios = 0;
        return cargarUsuarios();
    }
    // OJO: Comprueba que la ruta de este endpoint es exactamente la de tu Java
    realizarBusqueda(`${API_URL}/roles/${idRol}/usuarios`, false);
};

// --- EL MOTOR CENTRAL DE BÚSQUEDA ---
// urlFetch: El endpoint al que atacamos
// esUnico: true (si buscamos ID/Correo devuelven 1 objeto), false (si buscamos Rol devuelve array)
async function realizarBusqueda(urlFetch, esUnico) {
    const tabla = document.getElementById('tablaUsuarios');
    tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-blue-500 animate-pulse font-bold">Buscando...</td></tr>`;

    try {
        // Lanzamos la búsqueda y además traemos los roles para mantener las "etiquetas de colores"
        const [resBusqueda, resRoles, resMapa] = await Promise.all([
            fetch(urlFetch, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_URL}/roles`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_URL}/roles/${idRol}/usuarios`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (resBusqueda.ok && resRoles.ok && resMapa.ok) {
            const dataBusqueda = await resBusqueda.json();
            const todosRoles = await resRoles.json();
            const mapaUsuariosRoles = await resMapa.json();

            // Homogeneizamos la respuesta: Si es un solo usuario, lo metemos en un Array [ ]
            const usuarios = esUnico ? [dataBusqueda] : dataBusqueda;

            // Diccionario de roles rápido
            const diccRoles = {};
            todosRoles.forEach(r => diccRoles[r.id_rol] = r.name || r.nombre);

            // Cambiamos los botones de paginación para indicar que estamos en modo Búsqueda
            document.getElementById('infoPagUsuarios').innerText = `Resultados de Búsqueda`;
            document.getElementById('btnAntUsu').disabled = true;
            document.getElementById('btnSigUsu').disabled = true;

            tabla.innerHTML = '';

            if (usuarios.length === 0) {
                tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-gray-500 font-bold">No se encontraron resultados.</td></tr>`;
                return;
            }

            // Pintamos la tabla reciclando tu lógica de diseño
            usuarios.forEach(u => {
                const idUser = u.id_usuario || u.idUser;
                const nombreCompleto = `${u.nombre_usuario || u.nombreUsuario || ''} ${u.apellido_usuario || u.apellidoUsuario || ''}`.trim();
                const correo = u.correo_usuario || u.emailUsuario || 'Sin correo';

                const relacion = mapaUsuariosRoles.find(m => (m.id_usuario || m.idUser) === idUser);
                let rolesHtml = '<span class="text-gray-400 text-xs italic">Sin roles</span>';

                if (relacion && relacion.id_rol && relacion.id_rol.length > 0) {
                    rolesHtml = relacion.id_rol.map(idRol => {
                        const nombreRol = diccRoles[idRol] || `ID:${idRol}`;
                        return `<span class="bg-blue-100 text-blue-800 text-xs font-bold px-2 py-1 rounded border border-blue-200 mr-1 shadow-sm uppercase">${nombreRol}</span>`;
                    }).join('');
                }

                tabla.innerHTML += `
                    <tr class="hover:bg-blue-50 border-b border-gray-100 transition">
                        <td class="px-5 py-3 text-sm font-bold text-gray-800">${nombreCompleto}</td>
                        <td class="px-5 py-3 text-sm text-gray-600">${correo}</td>
                        <td class="px-5 py-3 text-center">${rolesHtml}</td>
                        <td class="px-5 py-3 text-center">
                            <button onclick="editarUsuario(${idUser})" class="text-blue-600 hover:text-blue-900 font-bold mr-3" title="Editar Usuario"><i class="fas fa-edit"></i></button>
                            <button onclick="abrirGestionRoles(${idUser})" class="text-purple-600 hover:text-purple-900 font-bold mr-3" title="Gestionar Roles"><i class="fas fa-user-tag"></i></button>
                            <button onclick="borrarUsuario(${idUser})" class="text-red-600 hover:text-red-900 font-bold" title="Eliminar Usuario"><i class="fas fa-trash"></i></button>
                        </td>
                    </tr>
                `;
            });
        } else {
            tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-gray-500 font-bold">Usuario no encontrado.</td></tr>`;
        }
    } catch (error) {
        tabla.innerHTML = `<tr><td colspan="4" class="text-center py-8 text-red-500 font-bold">Fallo de conexión al buscar.</td></tr>`;
    }
}

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

let idUsuarioRolActual = null; // Guardamos a qué usuario le estamos tocando los roles

window.abrirGestionRoles = async function(idUsuario) {
    idUsuarioRolActual = idUsuario;
    document.getElementById('modalGestionRoles').classList.remove('hidden');
    // Asume que tienes un <ul> o <div> con id="listaRolesUsuario" en tu modal
    const contenedorRoles = document.getElementById('listaRolesUsuario');
    contenedorRoles.innerHTML = '<p class="text-gray-500">Cargando...</p>';

    try {
        // Pedimos TODOS los roles y la relación de ESTE usuario
        const [resTodosRoles, resMapa] = await Promise.all([
            fetch(`${API_URL}/roles`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_URL}/usuarios_roles`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (resTodosRoles.ok && resMapa.ok) {
            const todosRoles = await resTodosRoles.json();
            const mapaCompleto = await resMapa.json();

            // Buscamos los roles de nuestro usuario específico
            const relacion = mapaCompleto.find(m => (m.id_usuario || m.idUser) === idUsuario);
            const rolesDelUsuario = relacion && relacion.id_rol ? relacion.id_rol : [];

            contenedorRoles.innerHTML = '';

            // Pintamos cada rol con un botón de Añadir o Quitar
            todosRoles.forEach(rol => {
                const idRol = rol.idRol || rol.id_rol;
                const nombreRol = rol.name || rol.nombre;
                const loTiene = rolesDelUsuario.includes(idRol);

                if (loTiene) {
                    contenedorRoles.innerHTML += `
                        <div class="flex justify-between items-center p-2 bg-green-50 border border-green-200 mb-2 rounded">
                            <span class="font-bold text-green-800 uppercase">${nombreRol}</span>
                            <button onclick="quitarRolAUsuario(${idUsuario}, ${idRol})" class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-xs font-bold transition">Quitar</button>
                        </div>
                    `;
                } else {
                    contenedorRoles.innerHTML += `
                        <div class="flex justify-between items-center p-2 bg-gray-50 border border-gray-200 mb-2 rounded">
                            <span class="font-bold text-gray-600 uppercase">${nombreRol}</span>
                            <button onclick="anadirRolAUsuario(${idUsuario}, ${idRol})" class="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-xs font-bold transition">Añadir (id: ${idRol})</button>
                        </div>
                    `;
                }
            });
        }
    } catch (error) {
        contenedorRoles.innerHTML = '<p class="text-red-500">Error al cargar la gestión de roles.</p>';
    }
}

window.cerrarGestionRoles = function() {
    document.getElementById('modalGestionRoles').classList.add('hidden');
    idUsuarioRolActual = null;
    cargarUsuarios(); // Recargamos la tabla principal para ver los badges actualizados
}

// Lógica POST para Añadir
window.anadirRolAUsuario = async function(idUsuario, idRol) {
    try {
        // 1. Usamos tu endpoint exacto
        const response = await fetch(`${API_URL}/usuarios/${idUsuario}/roles`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            // 2. Enviamos solo el id_rol en el JSON, como pide tu backend
            body: JSON.stringify({ id_rol: idRol })
        });

        if (response.ok) {
            abrirGestionRoles(idUsuario); // Refresca el modal al instante
        } else {
            // Chivato por si el backend se queja de algo (ej. "El usuario ya tiene el rol")
            const errorText = await response.text();
            console.error("Fallo en el backend:", errorText);
            alert("Error al asignar el rol. Revisa la consola (F12) para más detalles.");
        }
    } catch (error) {
        alert("Fallo de red al intentar asignar rol.");
    }
}

// Lógica DELETE para Quitar
window.quitarRolAUsuario = async function(idUsuario, idRol) {
    if (!confirm("¿Seguro que quieres quitarle este rol al usuario?")) return;

    try {
        // Asegúrate de que tu backend tiene este endpoint DELETE exacto configurado
        const response = await fetch(`${API_URL}/usuarios/${idUsuario}/roles/${idRol}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            abrirGestionRoles(idUsuario); // Refresca el modal
        } else {
            alert("Error al quitar el rol.");
        }
    } catch (error) {
        alert("Fallo de red al intentar quitar rol.");
    }
}

window.crearRol = function() { alert("Abrir modal para crear rol llamando a POST /roles"); }
window.editarRol = function(id) { alert("Abrir modal para editar rol " + id + " llamando a PUT /roles/" + id); }
window.borrarRol = function(id) { alert("Llamar a DELETE /roles/" + id + " (Borrado lógico)"); }

// Arrancamos en la pestaña por defecto
cargarUsuarios();