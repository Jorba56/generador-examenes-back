# Generador de Exámenes - Backend API (Sprint 1)

Esta es la API RESTful del backend para el proyecto "Generador de Exámenes", desarrollada con Java y Spring Boot. 

Este repositorio contiene el trabajo correspondiente al Sprint 1. El objetivo principal de esta primera fase ha sido asentar unas bases sólidas, definir una arquitectura limpia y asegurar que la gestión de usuarios, roles y sus relaciones funcione a la perfección antes de añadir más complejidad al sistema.

## Características destacadas

* **Gestión de usuarios y roles:** Implementación de un CRUD completo. He utilizado borrado lógico (soft delete) en lugar de borrado físico para asegurar que no se pierdan datos históricos.
* **Relaciones Many-To-Many:** El sistema permite asignar, consultar y quitar roles a los usuarios.
* **Uso de DTOs y MapStruct:** Para evitar problemas de seguridad, las entidades de la base de datos están totalmente aisladas. La API solo devuelve y recibe exactamente los datos que necesita el cliente.
* **Código limpio y mantenible:** Se han aplicado buenas prácticas de refactorización, como el uso de cláusulas de guarda para evitar anidamientos complejos y métodos seguros para el manejo de colecciones.
* **Alta cobertura de tests:** Todo el código crítico está respaldado por pruebas unitarias usando JUnit y Mockito. Se han contemplado tanto los escenarios ideales de funcionamiento como el manejo de errores (por ejemplo, buscar IDs que no existen en la base de datos).

## Arquitectura

El proyecto sigue una estructura clásica de tres capas para mantener el código desacoplado y escalable:
1. **Controllers:** Se encargan exclusivamente de recibir las peticiones HTTP y devolver las respuestas.
2. **Services:** Aquí reside toda la lógica de negocio y las validaciones.
3. **Repositories:** Interfaces de Spring Data JPA encargadas de la persistencia y comunicación con la base de datos.

---

## Endpoints de la API

### Usuarios (/usuarios)
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| GET | `/usuarios` | Obtiene la lista de todos los usuarios activos. |
| GET | `/usuarios/{id}` | Busca un usuario concreto por su ID. |
| POST | `/usuarios` | Crea un nuevo usuario. |
| PUT | `/usuarios/{rolEditor}/{id}` | Modifica los datos de un usuario existente. |
| DELETE | `/usuarios/{id}` | Realiza un borrado lógico del usuario. |

### Roles (/usuarios/roles)
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| GET | `/usuarios/roles` | Obtiene todos los roles activos del sistema. |
| GET | `/usuarios/roles/{id}` | Busca un rol por su ID. |
| POST | `/usuarios/roles` | Crea un nuevo rol. |
| PUT | `/usuarios/roles/{id}` | Modifica el nombre o estado de un rol. |
| DELETE | `/usuarios/roles/{id}` | Realiza un borrado lógico del rol. |

### Relaciones Usuario-Rol
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| POST | `/usuarios/{id}/roles` | Asigna un rol específico a un usuario. |
| DELETE | `/usuarios/{id_usuario}/roles/{id_rol}` | Le quita un rol a un usuario. |
| GET | `/usuarios/{id}/roles` | Consulta todos los roles que tiene asignados un usuario. |
| GET | `/usuarios/roles/{id_rol}/usuarios` | Consulta qué usuarios poseen un rol concreto. |
| GET | `/usuarios_roles` | Obtiene una lista plana con todas las asignaciones actuales. |

---

## Stack Tecnológico

* Java 21+
* Spring Boot 4.0.2
* Spring Data JPA / Hibernate
* MapStruct
* JUnit 6 y Mockito
* Maven

---

## Cómo ejecutar el proyecto en local

1. Clona este repositorio en tu equipo:
   ```bash
   git clone [https://github.com/Jorba56/generador-examenes-back.git]
