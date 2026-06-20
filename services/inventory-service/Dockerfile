FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY services/inventory-service/target/inventory-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
