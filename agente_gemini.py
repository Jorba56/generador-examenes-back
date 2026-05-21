#!/usr/bin/env python3
"""
Agente Gemini para generación de código Java
=============================================
Lee tus archivos .java y genera tests unitarios, documentación
o análisis usando la API de Gemini.

Uso:
    python agente_gemini.py --modo tests --clase EvaluacionServiceImpl
    python agente_gemini.py --modo tests --clase EvaluacionServiceImpl --microservicio examenes-service
    python agente_gemini.py --modo docs --clase ExamenController
    python agente_gemini.py --modo analisis --clase ReporteServiceImpl
    python agente_gemini.py --modo tests --todo
"""

import os
import sys
import json
import argparse
import requests
from pathlib import Path

# ============================================================
# CONFIGURACIÓN — edita estas variables
# ============================================================
GEMINI_API_KEY = "AIzaSyBCuAesU-c9qJu3ruf2FHqM-adWxUB7pOw"
GEMINI_URL = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key={GEMINI_API_KEY}"

# Ruta raíz de tu proyecto (donde está el pom.xml raíz)
# Cambia esta ruta a donde tengas tu proyecto
RUTA_PROYECTO = r"C:\Users\6003609\Downloads\sprint\sprintDef\generador-examenes-back"
# ============================================================


def buscar_archivo_java(nombre_clase: str, microservicio: str = None) -> Path | None:
    """Busca un archivo .java por nombre de clase en el proyecto."""
    ruta_base = Path(RUTA_PROYECTO)

    if microservicio:
        ruta_base = ruta_base / microservicio

    for archivo in ruta_base.rglob(f"{nombre_clase}.java"):
        # Ignorar carpetas target y test existentes
        if "target" not in str(archivo) and "test" not in str(archivo).lower():
            return archivo

    return None


def buscar_todos_java(microservicio: str = None) -> list[Path]:
    """Busca todos los archivos .java de producción en el proyecto."""
    ruta_base = Path(RUTA_PROYECTO)

    if microservicio:
        ruta_base = ruta_base / microservicio

    archivos = []
    for archivo in ruta_base.rglob("*.java"):
        ruta_str = str(archivo)
        # Solo archivos de src/main, ignorar target y tests existentes
        if "src\\main" in ruta_str or "src/main" in ruta_str:
            if "target" not in ruta_str:
                archivos.append(archivo)

    return archivos


def leer_contexto_proyecto(microservicio: str = None) -> str:
    """Lee todos los .java del proyecto para dar contexto completo a Gemini."""
    archivos = buscar_todos_java(microservicio)

    if not archivos:
        return ""

    contexto = []
    for archivo in archivos:
        try:
            contenido = archivo.read_text(encoding="utf-8")
            # Ruta relativa para que Gemini entienda la estructura
            ruta_relativa = archivo.relative_to(Path(RUTA_PROYECTO))
            contexto.append(f"// === ARCHIVO: {ruta_relativa} ===\n{contenido}")
        except Exception as e:
            print(f"  ⚠️  No se pudo leer {archivo.name}: {e}")

    print(f"  📁 Contexto: {len(archivos)} archivos leídos")
    return "\n\n".join(contexto)


def determinar_ruta_salida(archivo_fuente: Path, modo: str) -> Path:
    """Determina dónde guardar el archivo generado."""
    ruta_str = str(archivo_fuente)

    if modo == "tests":
        # Cambia src/main/java por src/test/java
        ruta_test = ruta_str.replace("src\\main\\java", "src\\test\\java")
        ruta_test = ruta_test.replace("src/main/java", "src/test/java")
        nombre_test = archivo_fuente.stem + "Test.java"
        return Path(ruta_test).parent / nombre_test

    elif modo == "docs":
        # Guarda la documentación en una carpeta docs/ junto al archivo
        return archivo_fuente.parent / f"{archivo_fuente.stem}_docs.md"

    elif modo == "analisis":
        return archivo_fuente.parent / f"{archivo_fuente.stem}_analisis.md"

    return archivo_fuente.parent / f"{archivo_fuente.stem}_generado.java"


def construir_prompt(modo: str, nombre_clase: str, codigo_clase: str, contexto_proyecto: str) -> str:
    """Construye el prompt según el modo seleccionado."""

    base_contexto = f"""
Contexto del proyecto completo (microservicio Spring Boot 4.x, Java 21):
{contexto_proyecto}

Clase a procesar ({nombre_clase}):
```java
{codigo_clase}
```
"""

    if modo == "tests":
        return f"""
Eres un experto en testing de Java con JUnit 5 y Mockito.

{base_contexto}

TAREA: Genera tests unitarios completos y exhaustivos para la clase {nombre_clase}.

REQUISITOS OBLIGATORIOS:
1. Usa JUnit 5 (@Test, @ExtendWith, @BeforeEach)
2. Usa Mockito para mockear dependencias (@Mock, @InjectMocks, when/verify)
3. Cubre casos: happy path, casos límite, excepciones esperadas
4. Nombres de test descriptivos en español: deberia_[accion]_cuando_[condicion]()
5. Un método @Test por caso de prueba
6. Imports completos y correctos
7. El test debe compilar sin errores

Genera SOLO el código Java del test, sin explicaciones adicionales.
Empieza directamente con el package y los imports.
"""

    elif modo == "docs":
        return f"""
Eres un experto en documentación de código Java.

{base_contexto}

TAREA: Genera documentación Javadoc completa para la clase {nombre_clase}.

REQUISITOS:
1. Javadoc para la clase con descripción, @author, @version
2. Javadoc para cada método público con @param, @return, @throws
3. Descripción clara de qué hace cada método
4. Ejemplos de uso donde sea relevante

Genera la documentación en formato Markdown con los bloques Javadoc incluidos.
"""

    elif modo == "analisis":
        return f"""
Eres un experto en análisis de código Java y buenas prácticas.

{base_contexto}

TAREA: Analiza la clase {nombre_clase} y proporciona:

1. RESUMEN: Qué hace esta clase y su responsabilidad en el sistema
2. PUNTOS FUERTES: Qué está bien implementado
3. PROBLEMAS DETECTADOS: Bugs potenciales, code smells, violaciones de SOLID
4. MEJORAS SUGERIDAS: Refactoring recomendado con ejemplos de código
5. COBERTURA DE TESTS: Qué casos críticos deberían tener tests

Sé específico y menciona líneas de código concretas cuando sea relevante.
Responde en español.
"""

    return f"Analiza este código Java y proporciona sugerencias de mejora:\n\n{codigo_clase}"


def llamar_gemini(prompt: str) -> str:
    """Llama a la API de Gemini y devuelve la respuesta."""
    body = {
        "contents": [
            {
                "parts": [{"text": prompt}]
            }
        ],
        "generationConfig": {
            "temperature": 0.2,  # Bajo para código más determinista
            "maxOutputTokens": 8192
        }
    }

    try:
        response = requests.post(
            GEMINI_URL,
            headers={"Content-Type": "application/json"},
            json=body,
            timeout=120  # 2 minutos de timeout
        )
        response.raise_for_status()

        data = response.json()
        return data["candidates"][0]["content"]["parts"][0]["text"]

    except requests.exceptions.Timeout:
        print("  ❌ Timeout — Gemini tardó más de 2 minutos en responder")
        sys.exit(1)
    except requests.exceptions.HTTPError as e:
        print(f"  ❌ Error HTTP de Gemini: {e.response.text}")
        sys.exit(1)
    except (KeyError, IndexError):
        print(f"  ❌ Respuesta inesperada de Gemini: {response.text[:500]}")
        sys.exit(1)



def limpiar_codigo(texto: str) -> str:
    """Elimina los bloques de código markdown si Gemini los añade."""
    texto = texto.strip()

    # Busca ```java con posibles espacios antes
    import re
    match = re.search(r'```(?:java)?\s*\n(.*?)```', texto, re.DOTALL)
    if match:
        return match.group(1).strip()

    return texto

def leer_contexto_relevante(archivo_clase: Path, microservicio: str = None) -> str:
    """Lee solo los archivos relevantes para la clase a testear."""
    codigo_clase = archivo_clase.read_text(encoding="utf-8")

    # Extrae los imports para saber qué clases usa
    import re
    imports = re.findall(r'import com\.jorge\.[^;]+;', codigo_clase)
    clases_usadas = []
    for imp in imports:
        # Extrae el nombre de la clase del import
        nombre = imp.split(".")[-1].replace(";", "").strip()
        clases_usadas.append(nombre)

    print(f"  📎 Clases relacionadas detectadas: {', '.join(clases_usadas)}")

    # Busca y lee esas clases
    ruta_base = Path(RUTA_PROYECTO)
    if microservicio:
        ruta_base = ruta_base / microservicio

    contexto = []
    for nombre_clase in clases_usadas:
        for archivo in ruta_base.rglob(f"{nombre_clase}.java"):
            if "target" not in str(archivo):
                try:
                    contenido = archivo.read_text(encoding="utf-8")
                    ruta_rel = archivo.relative_to(Path(RUTA_PROYECTO))
                    contexto.append(f"// === {ruta_rel} ===\n{contenido}")
                    print(f"  ✅ Contexto añadido: {nombre_clase}.java")
                except Exception:
                    pass

    return "\n\n".join(contexto)

def procesar_clase(nombre_clase: str, modo: str, microservicio: str = None):
    """Procesa una clase específica."""
    print(f"\n🔍 Buscando {nombre_clase}.java...")

    archivo = buscar_archivo_java(nombre_clase, microservicio)
    if not archivo:
        print(f"  ❌ No se encontró {nombre_clase}.java en el proyecto")
        if microservicio:
            print(f"     Buscado en: {RUTA_PROYECTO}\\{microservicio}")
        else:
            print(f"     Buscado en: {RUTA_PROYECTO}")
        sys.exit(1)

    print(f"  ✅ Encontrado: {archivo.relative_to(Path(RUTA_PROYECTO))}")

    codigo_clase = archivo.read_text(encoding="utf-8")

    print(f"  📖 Leyendo contexto del proyecto...")
    contexto = leer_contexto_relevante(archivo, microservicio)

    print(f"  🤖 Enviando a Gemini (modo: {modo})...")
    prompt = construir_prompt(modo, nombre_clase, codigo_clase, contexto)
    respuesta = llamar_gemini(prompt)
    print(f"  [DEBUG] Respuesta cruda: {respuesta[:200]}")
    # Limpia el markdown si es código Java
    if modo == "tests":
        respuesta = limpiar_codigo(respuesta)

    # Determina dónde guardar
    ruta_salida = determinar_ruta_salida(archivo, modo)

    # Crea la carpeta si no existe
    ruta_salida.parent.mkdir(parents=True, exist_ok=True)

    # Guarda el archivo
    ruta_salida.write_text(respuesta, encoding="utf-8")

    print(f"\n✅ Archivo generado: {ruta_salida}")
    print(f"   Tamaño: {len(respuesta)} caracteres")


def procesar_todo(modo: str, microservicio: str = None):
    """Procesa todas las clases del proyecto."""
    archivos = buscar_todos_java(microservicio)

    if not archivos:
        print("❌ No se encontraron archivos .java")
        sys.exit(1)

    print(f"\n📦 Se procesarán {len(archivos)} archivos")

    exitosos = 0
    fallidos = 0

    for archivo in archivos:
        nombre_clase = archivo.stem
        print(f"\n[{exitosos + fallidos + 1}/{len(archivos)}] Procesando {nombre_clase}...")

        try:
            codigo_clase = archivo.read_text(encoding="utf-8")
            contexto = leer_contexto_proyecto(microservicio)
            prompt = construir_prompt(modo, nombre_clase, codigo_clase, contexto)
            respuesta = llamar_gemini(prompt)

            if modo == "tests":
                respuesta = limpiar_codigo(respuesta)

            ruta_salida = determinar_ruta_salida(archivo, modo)
            ruta_salida.parent.mkdir(parents=True, exist_ok=True)
            ruta_salida.write_text(respuesta, encoding="utf-8")

            print(f"  ✅ Generado: {ruta_salida.name}")
            exitosos += 1

        except Exception as e:
            print(f"  ❌ Error procesando {nombre_clase}: {e}")
            fallidos += 1

    print(f"\n{'='*50}")
    print(f"✅ Exitosos: {exitosos}")
    print(f"❌ Fallidos: {fallidos}")
    print(f"{'='*50}")


def main():
    parser = argparse.ArgumentParser(
        description="Agente Gemini para generación de código Java",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Ejemplos:
  python agente_gemini.py --modo tests --clase EvaluacionServiceImpl
  python agente_gemini.py --modo tests --clase EvaluacionServiceImpl --microservicio examenes-service
  python agente_gemini.py --modo docs --clase ExamenController
  python agente_gemini.py --modo analisis --clase ReporteServiceImpl
  python agente_gemini.py --modo tests --todo --microservicio examenes-service
        """
    )

    parser.add_argument("--modo", required=True,
                        choices=["tests", "docs", "analisis"],
                        help="Qué generar: tests=tests unitarios, docs=documentación, analisis=análisis de código")

    parser.add_argument("--clase",
                        help="Nombre de la clase Java sin extensión (ej: EvaluacionServiceImpl)")

    parser.add_argument("--microservicio",
                        help="Nombre del microservicio (ej: examenes-service). Si no se especifica, busca en todo el proyecto")

    parser.add_argument("--todo", action="store_true",
                        help="Procesar todas las clases del proyecto")

    args = parser.parse_args()

    if not args.clase and not args.todo:
        print("❌ Debes especificar --clase NombreClase o --todo")
        parser.print_help()
        sys.exit(1)

    print("=" * 50)
    print("  🤖 Agente Gemini — Generador de código Java")
    print("=" * 50)
    print(f"  Modo:          {args.modo}")
    print(f"  Proyecto:      {RUTA_PROYECTO}")
    if args.microservicio:
        print(f"  Microservicio: {args.microservicio}")
    if args.clase:
        print(f"  Clase:         {args.clase}")
    if args.todo:
        print(f"  Modo:          TODO el proyecto")
    print("=" * 50)

    if args.todo:
        procesar_todo(args.modo, args.microservicio)
    else:
        procesar_clase(args.clase, args.modo, args.microservicio)


if __name__ == "__main__":
    main()