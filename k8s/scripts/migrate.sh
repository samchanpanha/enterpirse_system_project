#!/usr/bin/env bash
set -euo pipefail

# migrate.sh — Apply K8s manifests in correct dependency order
# Usage: ./migrate.sh [--dry-run]
# Prerequisites: kubectl configured with target cluster context

NAMESPACE="report-system"
DRY_RUN="${1:+--dry-run=client}"

echo "=== Report System K8s Migration ==="
echo "Namespace: $NAMESPACE"
echo "Dry-run: ${DRY_RUN:-no}"
echo ""

apply() {
  echo "→ $1"
  kubectl apply -f "$1" -n "$NAMESPACE" $DRY_RUN
}

# 1. Namespace (must exist first)
echo "--- Phase 1: Namespace ---"
kubectl apply -f k8s/namespace.yaml $DRY_RUN

# 2. Config & Secrets (dependencies for all services)
echo ""
echo "--- Phase 2: Configuration ---"
apply "k8s/configmap.yaml"
apply "k8s/secrets.yaml"

# 3. Infrastructure: Kafka + Zookeeper (event bus)
echo ""
echo "--- Phase 3: Event Bus ---"
apply "k8s/kafka-stack.yaml"

# 4. Databases (per-service PostgreSQL)
echo ""
echo "--- Phase 4: Databases ---"
apply "k8s/postgres-stack.yaml"

# 5. Service Discovery (Eureka)
echo ""
echo "--- Phase 5: Service Discovery ---"
apply "k8s/service-stack.yaml"  # Eureka first within this file

# 6. API Gateway
echo ""
echo "--- Phase 6: API Gateway ---"
# Gateway is in service-stack.yaml, applied in phase 5

# 7. Wait for Eureka before deploying services
echo ""
echo "--- Phase 7: Waiting for Eureka ---"
kubectl wait --for=condition=available --timeout=120s deployment/eureka -n "$NAMESPACE" $DRY_RUN || echo "Warning: eureka not ready, continuing..."

# 8. Microservices (all 7)
echo ""
echo "--- Phase 8: Microservices ---"
# Already in service-stack.yaml, applied in phase 5

# 9. Frontend (Nuxt)
echo ""
echo "--- Phase 9: Frontend ---"
# Nuxt is at end of service-stack.yaml

# 10. Ingress + HPA
echo ""
echo "--- Phase 10: Ingress & Autoscaling ---"
apply "k8s/ingress.yaml"
apply "k8s/hpa.yaml"

echo ""
echo "=== Migration Complete ==="
echo "Monitor rollout:"
echo "  kubectl get pods -n $NAMESPACE -w"
echo ""
echo "Check status:"
echo "  kubectl get all -n $NAMESPACE"
