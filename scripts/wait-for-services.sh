#!/bin/bash
# Wait for all services to be healthy before proceeding
set -e

SERVICES=(
  "http://localhost:8761/actuator/health"   # Eureka
  "http://localhost:8080/actuator/health"    # Gateway
  "http://localhost:8081/actuator/health"    # Auth Service
  "http://localhost:8082/actuator/health"    # Property Service
  "http://localhost:8083/actuator/health"    # Restaurant Service
  "http://localhost:8084/actuator/health"    # Inventory Service
  "http://localhost:8085/actuator/health"    # Finance Service
  "http://localhost:8086/actuator/health"    # Payment Service
  "http://localhost:8087/actuator/health"    # Reporting Service
)

echo "Waiting for services..."
for url in "${SERVICES[@]}"; do
  service_name=$(echo $url | awk -F/ '{print $3}')
  echo "  Waiting for $service_name..."
  until curl -sf $url > /dev/null 2>&1; do
    printf '.'
    sleep 2
  done
  echo "  $service_name is ready!"
done

echo "All services are healthy."
