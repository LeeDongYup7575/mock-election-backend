FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY target/app.jar app.jar
COPY gcs-key.json gcs-key.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]