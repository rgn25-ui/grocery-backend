FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY target/grocery-backend-1.0.0.jar app.jar
EXPOSE 8080

RUN apk add --no-cache curl

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]