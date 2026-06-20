FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl tzdata
WORKDIR /app
COPY services/auth-service/target/auth-service-*.jar app.jar
EXPOSE 8081
# Add JVM optimizations for containerized environment:
# - Use container-aware heap sizing
# - Enable G1GC for low-latency garbage collection
# - Add Spring Boot AOT optimizations if applicable
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:G1HeapRegionSize=16m", \
  "-Xms256m", \
  "-Xmx512m", \
  "-jar", "app.jar"]