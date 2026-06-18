FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY infrastructure/gateway/target/gateway-server-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
