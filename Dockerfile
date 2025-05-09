FROM openjdk:17-jdk-slim

# GCS 키 복사
COPY gcs-key.json /app/gcs-key.json

# JAR 복사
COPY src/main/resources/gcs-key.json /app/gcs-key.json

# GCS 환경변수 설정
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/gcs-key.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
