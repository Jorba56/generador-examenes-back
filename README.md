# Generador de Exámenes - Backend API (Sprint 1 y 2)

Esta es la API RESTful del backend para el proyecto "Generador de Exámenes", desarrollada con Java y Spring Boot. 

Este repositorio contiene el trabajo correspondiente hasta el **Sprint 2**. Tras asentar unas bases sólidas de CRUD y arquitectura limpia en la primera fase, el objetivo de este segundo sprint ha sido implementar una capa de seguridad robusta, un sistema automatizado de auditoría de errores (incidencias) y características avanzadas de resiliencia y monitorización.

## Características destacadas

* **Seguridad integral con JWT (Spring Security):** Implementación de un sistema de autenticación sin estado (stateless). Las rutas están fuertemente protegidas mediante Control de Acceso Basado en Roles (RBAC) y validaciones de negocio estrictas (un usuario normal solo puede editar su perfil, un admin no puede ver contraseñas en claro).
* **Subsistema Automático de Incidencias:** Un interceptor global (`@RestControllerAdvice`) captura cualquier excepción del sistema o error de negocio, devuelve un JSON estandarizado al cliente y guarda de forma silenciosa un registro detallado en la base de datos (Clase, Método, Stacktrace parcial y Usuario responsable).
* **Logs Centralizados no intrusivos:** Uso de Programación Orientada a Aspectos (Spring AOP) para interceptar las llamadas a los servicios y registrar los tiempos de ejecución en milisegundos mediante SLF4J, escupiendo los datos rotativamente a un archivo `.txt` local.
* **Resiliencia con Circuit Breaker:** Implementación del patrón Circuit Breaker mediante *Resilience4j* en los puntos críticos de consulta, asegurando métodos de respaldo (`fallbacks`) si la base de datos deja de responder.
* **Gestión de Usuarios y Roles:** CRUD completo con borrado lógico (soft delete) para mantener el histórico de datos intacto, usando DTOs y MapStruct para aislar las entidades de la base de datos de la vista pública. Configuración estricta en `snake_case` para el intercambio de JSON.
* **Alta cobertura de tests:** Pruebas unitarias avanzadas con JUnit y Mockito, mockeando incluso el contexto de seguridad (`Authentication`) para validar las reglas de negocio.

## Arquitectura

El proyecto sigue una estructura multicapa para mantener el código desacoplado y escalable:
1. **Controllers:** Se encargan exclusivamente de recibir las peticiones HTTP y devolver respuestas (DTOs).
2. **Services:** Centralizan toda la lógica de negocio y las validaciones de acceso.
3. **Repositories:** Interfaces de Spring Data JPA encargadas de la persistencia.
4. **Security & AOP:** Capa transversal que aloja los filtros de tokens (`JwtFilter`), la configuración de seguridad y los interceptores de rendimiento (`LoggingAspect`).

---

## Endpoints de la API

### Autenticación (/auth) - *Públicos*
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| POST | `/auth/login` | Valida credenciales y devuelve un Token JWT de sesión. |
| POST | `/auth/register` | Registra un nuevo usuario en el sistema (Rol ALUMNO por defecto). |
| POST | `/auth/refresh` | Renueva un token de sesión antes de que caduque. |

### Usuarios (/usuarios) - *Protegidos*
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| GET | `/usuarios` | Obtiene la lista de todos los usuarios activos. |
| GET | `/usuarios/{id}` | Busca un usuario concreto por su ID. |
| POST | `/usuarios` | Crea un nuevo usuario. |
| PUT | `/usuarios/{id}` | Modifica los datos del usuario (Aplica reglas ABAC según el Token). |
| DELETE | `/usuarios/{id}` | Realiza un borrado lógico del usuario. |

### Roles (/usuarios/roles) - *Solo Administradores*
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| GET | `/usuarios/roles` | Obtiene todos los roles activos del sistema. |
| GET | `/usuarios/roles/{id}` | Busca un rol por su ID. |
| POST | `/usuarios/roles` | Crea un nuevo rol. |
| PUT | `/usuarios/roles/{id}` | Modifica el nombre o estado de un rol. |
| DELETE | `/usuarios/roles/{id}` | Realiza un borrado lógico del rol. |

### Relaciones Usuario-Rol - *Solo Administradores*
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| POST | `/usuarios/{id}/roles` | Asigna un rol específico a un usuario. |
| DELETE | `/usuarios/{id_usuario}/roles/{id_rol}` | Le quita un rol a un usuario. |
| GET | `/usuarios/{id}/roles` | Consulta todos los roles que tiene asignados un usuario. |
| GET | `/usuarios/roles/{id_rol}/usuarios` | Consulta qué usuarios poseen un rol concreto. |
| GET | `/usuarios_roles` | Obtiene una lista plana optimizada con todas las asignaciones. |

### Incidencias (/incidencias) - *Solo Administradores*
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| GET | `/incidencias` | Obtiene el historial completo de errores de la API. |
| GET | `/incidencias/{id}` | Busca una incidencia por ID (Protegido con Circuit Breaker). |
| GET | `/incidencias/usuario/{idUsuario}` | Historial de errores provocados por un usuario específico. |
| GET | `/incidencias/clase/{clase}` | Filtra incidencias originadas en una clase concreta. |
| GET | `/incidencias/metodo/{metodo}` | Filtra incidencias originadas en un método concreto. |

---

## Stack Tecnológico

* Java 21+
* Spring Boot 4.0.2
* **Spring Security + JWT (io.jsonwebtoken)**
* **Spring AOP (AspectJ)**
* **Resilience4j (Circuit Breaker)**
* Spring Data JPA / Hibernate
* MapStruct
* JUnit 6 y Mockito
* Maven

---

## Cómo ejecutar el proyecto en local

1. Clona este repositorio en tu equipo:
   ```bash
   git clone [https://github.com/Jorba56/generador-examenes-back.git](https://github.com/Jorba56/generador-examenes-back.git)
