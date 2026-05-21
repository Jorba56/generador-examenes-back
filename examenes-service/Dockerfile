# Usamos la imagen oficial y ligera de java 21
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# INSTALACIÓN VITAL: Sin estas fuentes, el motor PDF de BIRT explota en Linux
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    libfreetype6 \
    fonts-dejavu-core \
    && fc-cache -f -v \
    && rm -rf /var/lib/apt/lists/*

# Copiamos tu JAR compilado
COPY target/examenes-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

# MODO HEADLESS VITAL: Obliga a Java a no buscar una pantalla de Windows
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "app.jar"]