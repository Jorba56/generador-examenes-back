Aquí tienes la documentación completa y estructurada en Markdown para el script `agente_gemini.py`.

# Documentación: `agente_gemini.py`

## 1. Descripción de la clase y su responsabilidad en el sistema

El script `agente_gemini.py` actúa como un **Asistente de Inteligencia Artificial (CLI)** diseñado específicamente para interactuar con un proyecto backend basado en Java 21 y Spring Boot 4.x bajo una arquitectura de microservicios. 

**Responsabilidades principales:**
*   **Análisis de Contexto:** Escanea el sistema de archivos del proyecto para construir un contexto inteligente (leyendo clases, dependencias, configuraciones y propiedades) respetando los límites de tokens.
*   **Ingeniería de Prompts:** Construye dinámicamente prompts especializados para diferentes tareas de ingeniería de software (testing, refactorización, documentación, seguridad).
*   **Interacción con LLM:** Se comunica con la API de Google Gemini para procesar el contexto y generar respuestas precisas.
*   **Generación de Artefactos:** Crea y guarda automáticamente archivos en las rutas correctas del proyecto (ej. genera clases `*Test.java` en `src/test/java`, archivos `.md` para documentación, scripts `.sql`, o configuraciones de Docker).
*   **Modo Conversacional:** Proporciona una interfaz de chat interactiva en la terminal con memoria de contexto sobre el código fuente del proyecto.

---

## 2. Diagrama de dependencias (ASCII)

```text
                                  +------------------------------------+
                                  |        agente_gemini.py            |
                                  |  (Orquestador CLI y Lógica Core)   |
                                  +------------------------------------+
                                     /              |               \
                                    /               |                \
      +-----------------------------+    +--------------------+    +-----------------------------+
      |      Sistema de Archivos    |    |   API de Gemini    |    |       Módulos Python        |
      |      (Proyecto Spring Boot) |    | (Google GenAI API) |    | (os, sys, re, json, pathlib)|
      +-----------------------------+    +--------------------+    +-----------------------------+
      | - Lee: .java, .yml, .sql    |    | - Recibe: Prompts  |    | - requests (HTTP calls)     |
      | - Escribe: *Test.java, .md  |    | - Devuelve: Código |    | - argparse (CLI args)       |
      | - Ignora: target, .git      |    |   y Análisis       |    | - readline (Chat history)   |
      +-----------------------------+    +--------------------+    +-----------------------------+
```

---

## 3. Tabla de métodos principales

Aunque es un script de Python, a continuación se detallan sus funciones principales (equivalentes a métodos públicos) con sus firmas y propósitos:

| Método / Función | Parámetros | Retorno | Descripción |
| :--- | :--- | :--- | :--- |
| `buscar_archivo` | `nombre: str`, `microservicio: str`, `solo_main: bool` | `Path \| None` | Busca un archivo específico en el proyecto, ignorando carpetas de compilación. |
| `buscar_todos` | `microservicio: str`, `extensiones: set`, `solo_main: bool` | `list[Path]` | Recupera todos los archivos de código válidos dentro de un microservicio. |
| `contexto_clase` | `archivo: Path`, `microservicio: str` | `str` | Lee una clase y busca automáticamente sus dependencias (imports locales) para armar un contexto enriquecido. |
| `contexto_proyecto_completo`| `microservicio: str`, `extensiones: set` | `str` | Concatena todo el código del proyecto/microservicio controlando el límite máximo de tokens (`MAX_TOKENS_CTX`). |
| `llamar_gemini` | `prompt: str`, `historial: list` | `str` | Ejecuta la petición HTTP POST a la API de Gemini manejando timeouts y errores. |
| `limpiar_codigo` | `texto: str` | `str` | Extrae el código fuente puro eliminando los bloques de formato Markdown (ej. ` ```java `) devueltos por la IA. |
| `ruta_salida` | `archivo: Path`, `modo: str`, `sufijo: str` | `Path` | Calcula la ruta de destino para el archivo generado (ej. mapea `src/main` a `src/test` para los tests). |
| `guardar` | `ruta: Path`, `contenido: str` | `None` | Crea los directorios necesarios y guarda el contenido generado en el disco. |
| `modo_clase` | `nombre: str`, `modo: str`, `microservicio: str`| `None` | Orquesta el flujo completo para operar sobre una sola clase (buscar -> contexto -> IA -> guardar). |
| `modo_chat` | `microservicio: str` | `None` | Inicia un bucle interactivo REPL en la terminal manteniendo el contexto del proyecto en memoria. |

---

## 4. Ejemplos de uso con código

Al ser una herramienta CLI, su uso se realiza a través de la terminal. Aquí tienes los ejemplos de ejecución:

**1. Generar Tests Unitarios (JUnit 5 + Mockito) para una clase específica:**
```bash
python agente_gemini.py --modo tests --clase EvaluacionServiceImpl --microservicio examenes-service
```

**2. Realizar una auditoría de seguridad en todo un microservicio:**
```bash
python agente_gemini.py --modo seguridad --todo --microservicio usuarios-service
```

**3. Generar un Dockerfile optimizado y un docker-compose.yml:**
```bash
python agente_gemini.py --modo dockerfile
```

**4. Iniciar el modo chat interactivo para consultar dudas sobre la arquitectura:**
```bash
python agente_gemini.py --modo chat --microservicio gateway-service
```

---

## 5. Docstrings (Equivalente a Javadoc en Python)

A continuación, se presentan los comentarios de documentación en formato **Docstring (PEP 257)** listos para ser copiados y pegados en las funciones principales del archivo `agente_gemini.py`.

```python
def buscar_archivo(nombre: str, microservicio: str = None, solo_main: bool = True) -> Path | None:
    """
    Busca un archivo por su nombre dentro del árbol de directorios del proyecto.
    
    Realiza una búsqueda recursiva ignorando directorios de compilación y control de versiones.
    Si el nombre no incluye extensión, asume por defecto la búsqueda de archivos .java o .py.
    
    Args:
        nombre (str): Nombre del archivo a buscar (con o sin extensión).
        microservicio (str, opcional): Nombre del directorio del microservicio para acotar la búsqueda.
        solo_main (bool, opcional): Si es True, excluye los archivos ubicados en carpetas de test. Por defecto es True.
        
    Returns:
        Path | None: La ruta absoluta del archivo si se encuentra, o None si no existe.
    """
    # ... código existente ...

def contexto_clase(archivo: Path, microservicio: str = None) -> str:
    """
    Construye un contexto inteligente para la IA leyendo la clase objetivo y sus dependencias directas.
    
    Analiza las sentencias 'import' del archivo proporcionado para detectar qué otras clases
    del mismo proyecto están siendo utilizadas. Luego, busca y adjunta el código fuente de 
    esas dependencias para proporcionar a la IA una visión completa del ecosistema de la clase.
    
    Args:
        archivo (Path): Ruta del archivo principal a analizar.
        microservicio (str, opcional): Nombre del microservicio para limitar la búsqueda de dependencias.
        
    Returns:
        str: Una cadena de texto formateada que contiene el código de la clase y sus dependencias,
             separadas por comentarios indicando la ruta de cada archivo.
    """
    # ... código existente ...

def llamar_gemini(prompt: str, historial: list = None) -> str:
    """
    Realiza una petición HTTP a la API de Google Gemini para generar contenido.
    
    Configura los parámetros de generación (temperatura, tokens máximos) y maneja
    la comunicación con el modelo LLM. Soporta tanto peticiones de un solo turno (zero-shot)
    como conversaciones multi-turno si se proporciona un historial.
    
    Args:
        prompt (str): La instrucción o pregunta principal a enviar al modelo.
        historial (list, opcional): Lista de diccionarios con el formato de mensajes de Gemini 
                                    para mantener el contexto en el modo chat.
                                    
    Returns:
        str: El texto generado por el modelo de IA.
        
    Raises:
        SystemExit: Si ocurre un timeout, un error HTTP o una respuesta malformada, 
                    imprime el error en consola y termina la ejecución del script.
    """
    # ... código existente ...

def ruta_salida(archivo: Path, modo: str, sufijo: str = None) -> Path:
    """
    Calcula dinámicamente la ruta de destino donde se guardará el artefacto generado por la IA.
    
    Aplica reglas de negocio basadas en el 'modo' de ejecución. Por ejemplo, si el modo es 'tests',
    transforma la ruta de 'src/main/java' a 'src/test/java' y añade el sufijo 'Test' al nombre del archivo.
    Para modos de documentación, cambia la extensión a '.md'.
    
    Args:
        archivo (Path): Ruta del archivo original sobre el que se está operando.
        modo (str): El modo de ejecución actual (ej. 'tests', 'docs', 'analisis').
        sufijo (str, opcional): Un sufijo personalizado para añadir al nombre del archivo.
        
    Returns:
        Path: La ruta absoluta calculada donde debe escribirse el nuevo archivo.
    """
    # ... código existente ...
```