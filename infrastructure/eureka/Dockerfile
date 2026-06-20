FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl tzdata
WORKDIR /app
COPY infrastructure/eureka/target/eureka-server-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:G1HeapRegionSize=16m", \
  "-XX:+UnlockExperimentalVMOptions", \
  "-XX:+UseStringDeduplication", \
  "-Xms256m", \
  "-Xmx512m", \
  "-jar", "app.jar"]