# Documentación Técnica: `api_chatbot.py`

## 1. Descripción de la clase y su responsabilidad en el sistema

El archivo `api_chatbot.py` define un **Microservicio de Asistente de Inteligencia Artificial (Agente ReAct)** dentro del Sistema de Gestión de Exámenes. 

**Responsabilidades principales:**
1. **Agente Inteligente:** Utiliza LangGraph y Google Gemini para interpretar intenciones del usuario y decidir qué herramientas utilizar.
2. **Integración SQL (Text-to-SQL):** Se conecta a la base de datos MySQL del sistema para consultar de forma autónoma y segura (solo `SELECT`) datos reales sobre alumnos, notas y exámenes.
3. **Motor RAG (Retrieval-Augmented Generation):** Procesa documentos PDF (guías, manuales), los vectoriza usando FAISS y permite al agente buscar información teórica.
4. **Gestión de Memoria:** Mantiene el contexto conversacional de los usuarios almacenando el historial de chat directamente en la base de datos MySQL.
5. **API REST:** Expone endpoints mediante FastAPI para ser consumidos por el frontend o a través del API Gateway del ecosistema Spring Cloud.

---

## 2. Diagrama de dependencias (ASCII)

```text
                                  ┌──────────────────────────────┐
                                  │  Cliente Web / API Gateway   │
                                  └──────────────┬───────────────┘
                                                 │ HTTP / REST
                                                 ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           FastAPI (api_chatbot.py)                              │
│                                                                                 │
│  ┌─────────────────┐           ┌─────────────────────────────────────────────┐  │
│  │ Endpoints REST  │◄─────────►│ LangChain ReAct Agent (Google Gemini 3.1)   │  │
│  └─────────────────┘           └─┬─────────────┬─────────────┬───────────────┘  │
│                                  │             │             │                  │
└──────────────────────────────────┼─────────────┼─────────────┼──────────────────┘
                                   │             │             │
           ┌───────────────────────▼─┐     ┌─────▼─────┐ ┌─────▼──────────────────┐
           │ Herramientas SQL        │     │ Memoria   │ │ Herramienta RAG        │
           │ (ver_tablas, esquema,   │     │ (Historial│ │ (buscar_en_documentos) │
           │ consultar_base_datos)   │     │ en MySQL) │ │                        │
           └──────────┬──────────────┘     └─────┬─────┘ └──────────┬─────────────┘
                      │                          │                  │
                      ▼                          ▼                  ▼
           ┌───────────────────────────────────────────┐ ┌────────────────────────┐
           │ Base de Datos MySQL (examenes)            │ │ FAISS VectorStore      │
           │ - Tablas de negocio (alumnos, notas...)   │ │ (Fragmentos de PDFs)   │
           │ - Tabla 'message_store' (historial)       │ └────────────────────────┘
           └───────────────────────────────────────────┘
```

---

## 3. Tabla de métodos públicos

### Endpoints REST (FastAPI)
| Método / Endpoint | Parámetros | Retorno | Descripción |
| :--- | :--- | :--- | :--- |
| `GET /` | Ninguno | `HTMLResponse` | Sirve la interfaz gráfica estática (`index.html`). |
| `POST /chat` | `PeticionChat` (JSON con `session_id`, `mensaje`) | `dict` (status, respuesta) | Procesa un mensaje del usuario, invoca al agente IA y devuelve la respuesta. |
| `GET /historial/{session_id}` | `session_id` (Path variable) | `dict` (status, historial) | Recupera el historial de mensajes de una sesión específica desde la BD. |
| `GET /chats` | Ninguno | `dict` (status, chats) | Devuelve una lista con todos los `session_id` únicos almacenados en la BD. |
| `GET /estado` | Ninguno | `dict` (status, modelo, bd, rag...) | Endpoint de *Healthcheck* que indica el estado de las conexiones y herramientas. |
| `POST /recargar-pdfs` | Ninguno | `dict` (status, rag_activo) | Fuerza la recarga y re-vectorización de los documentos PDF en la carpeta. |

### Herramientas del Agente (LangChain Tools)
| Herramienta | Parámetros | Retorno | Descripción |
| :--- | :--- | :--- | :--- |
| `ver_tablas` | Ninguno | `str` | Devuelve los nombres de las tablas disponibles en MySQL. |
| `ver_esquema` | `tabla` (str) | `str` | Devuelve el DDL y esquema de una tabla específica. |
| `consultar_base_datos`| `query_sql` (str) | `str` | Ejecuta un `SELECT` en MySQL y devuelve los resultados. Bloquea DML/DDL. |
| `buscar_en_documentos`| `pregunta` (str) | `str` | Realiza una búsqueda de similitud en FAISS y devuelve fragmentos de PDF. |

---

## 4. Ejemplos de uso con código

### Ejemplo 1: Enviar un mensaje al Chatbot (cURL)
```bash
curl -X POST "http://localhost:8000/chat" \
     -H "Content-Type: application/json" \
     -d '{
           "session_id": "sesion-java-123",
           "mensaje": "¿Cuántos alumnos han aprobado el examen de Spring Boot?"
         }'
```

### Ejemplo 2: Consumir el endpoint desde un servicio Java (Spring Boot / RestTemplate)
```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class ChatbotClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String CHATBOT_URL = "http://localhost:8000/chat";

    public String preguntarAlAgente(String sessionId, String mensaje) {
        Map<String, String> request = Map.of(
            "session_id", sessionId,
            "mensaje", mensaje
        );
        
        ResponseEntity<Map> response = restTemplate.postForEntity(CHATBOT_URL, request, Map.class);
        return (String) response.getBody().get("respuesta");
    }
}
```

---
