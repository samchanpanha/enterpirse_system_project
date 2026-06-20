FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl tzdata
WORKDIR /app
COPY services/reporting-service/target/reporting-service-*.jar app.jar
EXPOSE 8087
# Container-optimized JVM settings
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:G1HeapRegionSize=16m", \
  "-Xms256m", \
  "-Xmx512m", \
  "-jar", "app.jar"]