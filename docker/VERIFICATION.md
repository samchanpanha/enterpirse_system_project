# Docker Verification Process

This document outlines the verification process to ensure the application runs correctly and efficiently within the Docker environment after optimization.

## 1. Build Verification

### 1.1 Compile All Modules
```bash
cd /Users/samchanpanha/Desktop/Java/Report_System
mvn clean compile -q
```

### 1.2 Build JAR Packages
```bash
mvn package -DskipTests
```

## 2. Docker Image Build

### 2.1 Verify Dockerfiles
All service Dockerfiles use optimized JVM settings:
- `-XX:+UseContainerSupport` - Enables container-aware memory sizing
- `-XX:MaxRAMPercentage=75.0` - Uses 75% of available container memory
- `-XX:+UseG1GC` - Low-latency garbage collector
- `-XX:G1HeapRegionSize=16m` - Optimized for typical object sizes
- `-XX:+UseStringDeduplication` - Reduces memory footprint

### 2.2 Build Docker Images
```bash
# From project root
docker compose -f docker/docker-compose.yml build
```

## 3. Container Startup and Health Checks

### 3.1 Start Services
```bash
docker compose -f docker/docker-compose.yml up -d
```

### 3.2 Verify Service Health
```bash
# Check all containers are healthy
docker compose -f docker/docker-compose.yml ps

# Expected healthy status for all services:
# - postgres (pg_isready)
# - kafka-zookeeper (nc check)
# - kafka (broker-api-versions)
# - keycloak (health/ready endpoint)
# - eureka (actuator/health)
# - All 7 microservices (actuator/health)

```

## 4. Performance Optimization Verification

### 4.1 JVM Container Optimization
```bash
# Check JVM flags in running container
docker exec report-gateway jps -v

# Verify container support is enabled
docker exec report-gateway java -XX:+PrintFlagsFinal -version | grep -i container
```

### 4.2 Cache Verification
The OidcTokenValidator now uses:
- **JWKS cache**: 5-minute TTL for Keycloak public keys
- **Validation cache**: Token-based caching with 10K entry max
- **Branch cache**: 60-second TTL with ConcurrentHashMap

Test cache hit on repeated requests:
```bash
# Get a valid token first
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"Demo123!"}' | jq -r '.accessToken')

# Make multiple requests to same endpoint - check logs for cache hits
docker logs report-gateway 2>&1 | grep "cache hit"
```

### 4.3 Database Query Optimization
The StockServiceImpl now uses optimized queries:
- `getCurrentStock` uses database-level aggregation instead of loading all records
- Repository methods properly filter by tenantId, branchId, and productId

Verify with PostgreSQL query analysis:
```bash
docker exec report-postgres psql -U report_user -d inventory_db -c "
EXPLAIN ANALYZE 
SELECT SUM(quantity) FROM stock_entries WHERE product_id = '...' AND branch_id = '...';
"
```

## 5. Functional Testing

### 5.1 Run E2E Smoke Test
```bash
# Ensure sample data is seeded first
docker/scripts/seed-sample-data.sh

# Run smoke test
./docker/scripts/e2e-smoke-test.sh
```

### 5.2 Manual Verification Points
1. **JWT Validation**: Both OIDC (Keycloak) and legacy JWT tokens work
2. **Branch Context Filter**: 
   - Requests with allowed branch → HTTP 200
   - Requests with disallowed branch → HTTP 403
3. **Stock Transfer Flow**: DRAFT → SHIPPED → RECEIVED works correctly
4. **Kafka Integration**: Events flow between inventory and finance services

### 5.3 Health Endpoint Checks
```bash
# Each service should respond to:
curl http://localhost:{port}/actuator/health

# Ports:
# - 8761: Eureka
# - 8080: Gateway
# - 8081: Auth
# - 8082: Property
# - 8083: Restaurant
# - 8084: Inventory
# - 8085: Finance
# - 8086: Payment
# - 8087: Reporting
# - 8180: Keycloak
```

## 6. Performance Benchmarks

### 6.1 Startup Time
```bash
# Measure service startup time
time docker compose -f docker/docker-compose.yml up -d
# All services should be healthy within 2-3 minutes
```

### 6.2 Request Latency
```bash
# Test with wrk or ab
wrk -t4 -c50 -d30s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/auth/users
```

### 6.3 Memory Usage
```bash
# Check container memory usage
docker stats --no-stream
# Each service should stay under configured limits
```

## 7. Production Deployment Verification

### 7.1 Production Docker Compose
```bash
# Build and start production version
docker compose -f docker/docker-compose.prod.yml --project-directory . up -d
```

### 7.2 Health Checks Configuration
Production services have:
- `restart: always` policy
- Resource limits (2GB for Postgres)
- Proper health check intervals
- Environment variable injection

## 8. Troubleshooting

### 8.1 Common Issues
1. **Container exits with OOM**: Check memory limits in Docker
2. **JWKS fetch timeout**: Verify Keycloak is reachable from gateway
3. **Branch access denied**: Check user-branch assignments in auth DB
4. **Kafka lag**: Verify Kafka topics are created before service startup

### 8.2 Log Inspection
```bash
# Check service logs
docker logs report-gateway
docker logs report-auth
docker logs report-inventory

# Check Keycloak logs
docker logs report-keycloak
```

## 9. Verification Checklist

- [ ] All Dockerfiles have JVM container optimizations
- [ ] All services have health checks configured
- [ ] JWKS fetching uses WebClient (non-blocking)
- [ ] Branch context filter uses async HTTP calls
- [ ] Stock queries use database aggregation
- [ ] Multi-tenant filtering works on all queries
- [ ] E2E smoke test passes (22 checks)
- [ ] Services start within 3 minutes
- [ ] Memory usage stays under limits
- [ ] Response times < 200ms for cached requests