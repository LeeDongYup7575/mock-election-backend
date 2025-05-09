FROM openjdk:17-jdk-slim


# JAR 복사
COPY target/*.jar /app/app.jar



EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
