# usamos la imagen oficial y ligera de java 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/examenes-service-0.0.1-SNAPSHOT.jar app.jar

# exponemos su puerto correspondiente
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]