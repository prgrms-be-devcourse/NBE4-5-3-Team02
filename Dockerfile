FROM openjdk:21-slim

COPY /backend/build/libs/toolgether-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]