from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import os
import logging

from langchain_community.chat_message_histories import SQLChatMessageHistory
from sqlalchemy import create_engine, MetaData, select
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
from langchain_core.tools import tool
from langchain_community.document_loaders import PyPDFDirectoryLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain_community.utilities import SQLDatabase
from langgraph.prebuilt import create_react_agent

# ════════════════════════════════════════════════════════════
# # CONFIGURACIÓN
# ════════════════════════════════════════════════════════════
logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")
log = logging.getLogger(__name__)

GOOGLE_API_KEY = os.environ.get("GOOGLE_API_KEY", "AIzaSyBCuAesU-c9qJu3ruf2FHqM-adWxUB7pOw")
os.environ["GOOGLE_API_KEY"] = GOOGLE_API_KEY

URL_BBDD       = os.environ.get("DATABASE_URL", "mysql+pymysql://root:12345@localhost:3306/examenes")
CARPETA_PDFS   = os.environ.get("PDF_DIR", "documentos")
MODELO_LLM     = "gemini-3.1-pro-preview"        # Estable y rápido
MODELO_EMBED   = "models/gemini-embedding-2"       # Último modelo estable de Google
TEMPERATURA    = 0.7

# ════════════════════════════════════════════════════════════
# APP
# ════════════════════════════════════════════════════════════
app = FastAPI(title="Chatbot Agente IA", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

class PeticionChat(BaseModel):
    session_id: str
    mensaje: str


# ════════════════════════════════════════════════════════════
# A. BASE DE DATOS SQL
# ════════════════════════════════════════════════════════════
try:
    db = SQLDatabase.from_uri(URL_BBDD)
    log.info(f"✅ BD conectada: {URL_BBDD}")
except Exception as e:
    log.error(f"❌ Error conectando a BD: {e}")
    db = None


@tool
def ver_tablas() -> str:
    """Muestra todas las tablas disponibles en la base de datos.
    Llama a esta herramienta primero para saber qué tablas existen."""
    if db is None:
        return "Base de datos no disponible."
    try:
        tablas = db.get_usable_table_names()
        return f"Tablas disponibles: {', '.join(tablas)}"
    except Exception as e:
        return f"Error obteniendo tablas: {e}"


@tool
def ver_esquema(tabla: str) -> str:
    """Muestra las columnas y estructura de una tabla concreta.
    Úsala antes de hacer consultas para conocer los campos disponibles.

    Args:
        tabla: Nombre exacto de la tabla a inspeccionar.
    """
    if db is None:
        return "Base de datos no disponible."
    try:
        return db.get_table_info([tabla])
    except Exception as e:
        return f"Error obteniendo esquema de '{tabla}': {e}"


@tool
def consultar_base_datos(query_sql: str) -> str:
    """Ejecuta una consulta SQL SELECT en la base de datos.
    Usa esta herramienta para obtener datos reales: notas, alumnos, exámenes, etc.
    IMPORTANTE: Solo se permiten consultas SELECT. Nunca INSERT, UPDATE ni DELETE.

    Args:
        query_sql: Consulta SQL SELECT completa y válida para MySQL.
    """
    if db is None:
        return "Base de datos no disponible."
    sql = query_sql.strip()
    if not sql.upper().startswith("SELECT"):
        return "Error: Solo se permiten consultas SELECT por seguridad."
    try:
        resultado = db.run(sql)
        if not resultado:
            return "La consulta no devolvió resultados."
        return str(resultado)
    except Exception as e:
        return f"Error ejecutando SQL: {e}"


# ════════════════════════════════════════════════════════════
# B. RAG — LECTURA DE PDFs
# ════════════════════════════════════════════════════════════
os.makedirs(CARPETA_PDFS, exist_ok=True)
_retriever = None


def cargar_pdfs() -> None:
    """Carga los PDFs de la carpeta documentos/ y construye el índice vectorial."""
    global _retriever
    try:
        loader = PyPDFDirectoryLoader(CARPETA_PDFS)
        docs = loader.load()

        if not docs:
            log.info("📂 Carpeta 'documentos' vacía — RAG desactivado.")
            return

        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
        splits = splitter.split_documents(docs)

        embeddings = GoogleGenerativeAIEmbeddings(
            model=MODELO_EMBED,
            task_type="retrieval_document",
        )

        # Creamos el vectorstore fragmento a fragmento para evitar el límite de batch
        log.info(f"📄 Procesando {len(splits)} fragmentos uno a uno...")

        primer_split = splits[0]
        vectorstore = FAISS.from_texts(
            [primer_split.page_content],
            embeddings,
            metadatas=[primer_split.metadata]
        )

        # Añadimos el resto de uno en uno
        for split in splits[1:]:
            vectorstore.add_texts(
                [split.page_content],
                metadatas=[split.metadata]
            )

        _retriever = vectorstore.as_retriever(search_kwargs={"k": 3})
        log.info(f"✅ RAG cargado: {len(docs)} PDFs → {len(splits)} fragmentos")

    except Exception as e:
        log.error(f"❌ Error cargando PDFs: {e}")
        _retriever = None



cargar_pdfs()


@tool
def buscar_en_documentos(pregunta: str) -> str:
    """Busca información teórica en los documentos PDF cargados.
    Usa esta herramienta para preguntas sobre conceptos, guías, manuales o documentación.
    NO uses esta herramienta para datos de la base de datos.

    Args:
        pregunta: La pregunta o concepto a buscar en los documentos.
    """
    if _retriever is None:
        return "No hay documentos PDF cargados. Añade PDFs a la carpeta 'documentos' y reinicia el servidor."
    try:
        docs = _retriever.invoke(pregunta)
        if not docs:
            return "No se encontró información relevante en los documentos."
        fragmentos = [f"[Fragmento {i+1}]\n{d.page_content}" for i, d in enumerate(docs)]
        return "\n\n".join(fragmentos)
    except Exception as e:
        return f"Error buscando en documentos: {e}"


# ════════════════════════════════════════════════════════════
# C. AGENTE LangGraph
# ════════════════════════════════════════════════════════════
llm = ChatGoogleGenerativeAI(
    model=MODELO_LLM,
    temperature=TEMPERATURA,
    max_retries=2,
)

herramientas = [ver_tablas, ver_esquema, consultar_base_datos, buscar_en_documentos]

SYSTEM_PROMPT = """Eres un asistente corporativo experto en desarrollo de software con Java y Spring Boot.

CAPACIDADES:
- Tienes acceso a una Base de Datos MySQL con datos reales del proyecto.
- Tienes acceso a documentos PDF con documentación técnica.

REGLAS DE COMPORTAMIENTO:
1. Para preguntas sobre datos (notas, alumnos, exámenes, registros): 
   → Usa PRIMERO ver_tablas, luego ver_esquema, luego consultar_base_datos.
2. Para preguntas teóricas o de documentación:
   → Usa buscar_en_documentos.
3. NUNCA inventes datos. Si no encuentras información, dilo claramente.
4. Responde siempre en español de forma clara, estructurada y concisa.
5. Cuando muestres datos de la BD, formátalos de forma legible."""

agente = create_react_agent(
    model=llm,
    tools=herramientas,
    prompt=SYSTEM_PROMPT,
)

log.info(f"✅ Agente creado con modelo {MODELO_LLM} y {len(herramientas)} herramientas")


# ════════════════════════════════════════════════════════════
# D. ENDPOINTS
# ════════════════════════════════════════════════════════════
def obtener_memoria(session_id: str):
    # Conecta directamente con tu URL_BBDD y agrupa los mensajes por session_id
    return SQLChatMessageHistory(
        session_id=session_id,
        connection=URL_BBDD
    )

@app.get("/", response_class=HTMLResponse)
def leer_interfaz():
    """Sirve la interfaz web."""
    ruta_html = os.path.join(os.path.dirname(__file__), "index.html")
    try:
        with open(ruta_html, "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        return HTMLResponse("<h1>index.html no encontrado</h1>", status_code=404)


@app.post("/chat")
def chatear(peticion: PeticionChat):
    try:
        if not peticion.mensaje.strip():
            return {"status": "error", "respuesta": "Mensaje vacío."}

        memoria = obtener_memoria(peticion.session_id)

        mensajes = list(memoria.messages)
        mensajes.append(HumanMessage(content=peticion.mensaje))

        resultado = agente.invoke({"messages": mensajes})

        # Extrae el último AIMessage con texto
        respuesta_final = "Sin respuesta."
        for msg in reversed(resultado["messages"]):
            if isinstance(msg, AIMessage) and msg.content:
                if isinstance(msg.content, str):
                    respuesta_final = msg.content
                elif isinstance(msg.content, list):
                    partes = [b.get("text", "") for b in msg.content if isinstance(b, dict)]
                    respuesta_final = " ".join(p for p in partes if p)
                if respuesta_final:
                    break

        memoria.add_user_message(peticion.mensaje)
        memoria.add_ai_message(respuesta_final)

        return {"status": "ok", "respuesta": respuesta_final}

    except Exception as e:
        log.error(f"Error en /chat: {e}")
        return {"status": "error", "respuesta": f"Error interno: {str(e)}"}

@app.get("/historial/{session_id}")
def obtener_historial(session_id: str):
    """Devuelve los mensajes previos de un chat para pintarlos al cargar la web."""
    memoria = obtener_memoria(session_id)
    mensajes = []
    for m in memoria.messages:
        # Convertimos los objetos de LangChain a diccionarios simples para el frontend
        role = "user" if isinstance(m, HumanMessage) else "assistant"
        mensajes.append({"role": role, "content": m.content})
    return {"status": "ok", "historial": mensajes}

@app.get("/chats")
def listar_chats():
    """Busca en MySQL todos los IDs de sesión (chats) creados hasta la fecha."""
    try:
        engine = create_engine(URL_BBDD)
        metadata = MetaData()
        metadata.reflect(bind=engine)

        # Si la tabla aún no existe (porque no hay chats), devolvemos lista vacía
        if 'message_store' not in metadata.tables:
            return {"status": "ok", "chats": []}

        message_store = metadata.tables['message_store']
        with engine.connect() as conn:
            query = select(message_store.c.session_id).distinct()
            chats = [row[0] for row in conn.execute(query).fetchall()]
            return {"status": "ok", "chats": chats}
    except Exception as e:
        return {"status": "error", "respuesta": str(e)}

@app.get("/estado")
def estado():
    return {
        "status": "ok",
        "modelo": MODELO_LLM,
        "bd_conectada": db is not None,
        "rag_activo": _retriever is not None,
        "herramientas": [t.name for t in herramientas]
    }


@app.post("/recargar-pdfs")
def recargar_pdfs():

    cargar_pdfs()
    return {
        "status": "ok",
        "rag_activo": _retriever is not None
    }