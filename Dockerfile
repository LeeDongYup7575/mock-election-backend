FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY target/app.jar app.jar
COPY src/main/resources/gcs-key.json /workspace/gcs-key.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]