

## Dossier Técnico: Agente IA

**Automatización, Refactorización y API Integrada**

## 1. Resumen Ejecutivo

El presente documento certifica la finalización, refactorización y empaquetado del **Sistema de Asistencia mediante Inteligencia Artificial** para el proyecto "Generador de Exámenes". Se excluyen de este informe las implementaciones de sistemas conversacionales (Chatbots), centrándose de manera estricta en el ecosistema de automatización local contenido en el directorio de trabajo actual.

El sistema final se compone de una arquitectura dual: un núcleo de procesamiento avanzado (`agente_gemini.py`) y una capa de exposición RESTful (`api.py`) diseñada para actuar como Sidecar. Todo ello está preparado para su despliegue containerizado mediante el `Dockerfile` provisto, garantizando la inmutabilidad y portabilidad del código original en entornos Windows y Linux.

---

## 2. Núcleo del Agente (`agente_gemini.py`)

El núcleo del agente es una herramienta CLI desarrollada en Python 3.11 capaz de indexar archivos y comunicarse con la API de Google Gemini (modelo `gemini-3.1-pro-preview`).

### 2.1. Inyección Multi-Lenguaje

Tras la última refactorización, el algoritmo de búsqueda de archivos (`buscar_todos`) y lectura de contexto (`contexto_proyecto_completo`) fue optimizado. Se ha eliminado el filtro rígido que restringía el escaneo a la carpeta `src/main` para ficheros no pertenecientes a Java.

> 💡 **Mejora aplicada:** Ahora el agente soporta de forma nativa la lectura e indexación de archivos `.py`, `.sql`, `.md`, `.yaml`, `.json` y `.dockerfile`, permitiendo refactorizar o documentar scripts ubicados en la raíz del proyecto.

### 2.2. Modos de Operación Soportados

* **tests:** Generación de pruebas unitarias exhaustivas con JUnit 5 y Mockito.
* **chat:**  Modo conversacional con memoria.
* **docs:** Creación de Javadoc y documentación en Markdown.
* **analisis:** Auditoría de *code smells*, violaciones SOLID y bugs potenciales.
* **refactor:** Optimización de legibilidad, rendimiento y manejo de excepciones.
* **seguridad:** Auditoría de vulnerabilidades (Inyección SQL, JWT, CORS).
* **dockerfile / readme / sql / frontend:** Generación de infraestructura y entregables.

---

## 3. Capa de Exposición y Sidecar (`api.py`)

Para aislar la ejecución del CLI y permitir su consumo de forma universal, se ha desarrollado un contenedor web utilizando **FastAPI**. Esta API actúa como un puente (Sidecar) entre el entorno y el script maestro.

### 3.1. Adaptación de Entorno Dinámica

Al iniciarse el servidor (evento `startup`), FastAPI lee el código fuente de `agente_gemini.py` y realiza una transpilación en memoria de las rutas absolutas de Windows (`C:\Users\...`) a rutas compatibles con contenedores Linux (`/app/workspace`), generando un clon virtual llamado `agente_gemini_linux.py`. Esto permite que el código original de Windows permanezca intacto y agnóstico a la infraestructura.

### 3.2. Endpoint de Ejecución Asíncrona

| Método | Endpoint | Payload Esperado | Respuesta |
| --- | --- | --- | --- |
| **POST** | `/ejecutar` | `{ "modo": "tests", "clase": "EvaluacionServiceImpl", "todo": false }` | Log de la consola interceptado y código de estado. |

---

## 4. Empaquetado y Despliegue (`Dockerfile`)

El empaquetado del sistema se ha resuelto mediante un contenedor ligero basado en `python:3.11-slim`. La arquitectura respeta el principio de montaje de volúmenes, lo que permite a la API ejecutar operaciones de IA sobre el disco físico del host.

```dockerfile
FROM python:3.11-slim
WORKDIR /app

# Instalamos la API
RUN pip install fastapi uvicorn requests pydantic

# Copiamos todos tus archivos (incluyendo el intocable agente_gemini.py)
COPY . .

# Exponemos el puerto de la web
EXPOSE 8000

# Arrancamos la API
CMD ["uvicorn", "api:app", "--host", "0.0.0.0", "--port", "8000"]

```

> 🚀 **Comando de Despliegue:**
> ```bash
> docker run -d -p 8000:8000 -v "/ruta/al/proyecto:/app/workspace" mi-agente-web
> 
> ```
>
>