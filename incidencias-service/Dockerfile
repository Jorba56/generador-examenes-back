# Usamos la imagen oficial y ligera de Java 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/incidencias-service-0.0.1-SNAPSHOT.jar app.jar

# Exponemos su puerto correspondiente
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]