# Report System — Kubernetes Deployment

Production-grade Kubernetes manifests for the 7-microservice Report System.

## Architecture overview

```
                   ┌──────────────┐
   ┌───────────┐   │  API Gateway │   ┌──────────────────────────────────────┐
   │  Ingress  │──▶│  :8080       │──▶│  Eureka :8761 (service discovery)   │
   │  (nginx)  │   └──────────────┘   └──────────────────────────────────────┘
   └───────────┘           │              │         │         │        │
                           ▼              ▼         ▼         ▼        ▼
                       ┌───────┐    ┌────────┐ ┌─────────┐ ┌──────────┐ ...
                       │ auth  │    │property│ │restaurant│ │inventory │ ...
                       │ :8081 │    │ :8082  │ │  :8083   │ │  :8084   │
                       └───┬───┘    └───┬────┘ └────┬─────┘ └────┬─────┘
                           │            │            │            │
                       ┌───▼────┐  ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐
                       │ auth_db│  │ property │ │restaurant│ │inventory │
                       └────────┘  │   _db    │ │   _db    │ │   _db    │
                                   └──────────┘ └──────────┘ └──────────┘

   ┌─────────┐  ┌─────────┐  ┌──────────┐                ┌──────────────┐
   │ finance │  │ payment │  │reporting │ ── Kafka ──▶  │ 7 PostgreSQL │
   │  :8085  │  │  :8086  │  │  :8087   │                │  instances   │
   └────┬────┘  └────┬────┘  └────┬─────┘                └──────────────┘
        │            │            │
   ┌────▼────┐  ┌────▼────┐  ┌────▼─────┐
   │finance  │  │payment  │  │reporting │
   │  _db    │  │  _db    │  │  _db     │
   └─────────┘  └─────────┘  └──────────┘
```

## Manifests

| File | Purpose |
|------|---------|
| `namespace.yaml` | `report-system` namespace |
| `configmap.yaml` | DB URLs, Kafka brokers, Eureka URL, Nuxt public URL |
| `secrets.yaml` | POSTGRES creds, JWT secret, payment gateway keys (replace stubs!) |
| `postgres-stack.yaml` | 7 PostgreSQL pods (one per service) + PVCs |
| `kafka-stack.yaml` | Kafka + Zookeeper for event bus |
| `service-stack.yaml` | Eureka + Gateway + 7 services + Nuxt frontend |
| `ingress.yaml` | nginx ingress for `api.*` and `app.*` with TLS |
| `hpa.yaml` | HorizontalPodAutoscalers (gateway + 7 services) |
| `kustomization.yaml` | Kustomize entry point |
| `k3d/ingress-local.yaml` | Traefik ingress for local k3d clusters |
| `scripts/migrate.sh` | One-shot Flyway migration runner |

## Quick start (production)

```bash
# 1. Edit secrets — replace placeholder JWT + gateway keys
$EDITOR k8s/secrets.yaml

# 2. Apply all manifests
kubectl apply -k k8s/

# 3. Verify
kubectl -n report-system get pods
kubectl -n report-system get svc

# 4. Access
#   http://api.reportsystem.local   → Gateway
#   http://app.reportsystem.local   → Nuxt frontend
```

## Local development (k3d)

```bash
# Create cluster
k3d cluster create report-system -p 80:80 -p 443:443

# Build & import images
docker build -t reportsystem/eureka-server:latest -f infrastructure/eureka/Dockerfile .
docker build -t reportsystem/api-gateway:latest      -f infrastructure/gateway/Dockerfile .
docker build -t reportsystem/auth-service:latest     -f services/auth-service/Dockerfile .
# ... (repeat for all 7 services + frontend)
k3d image load reportsystem/* --cluster report-system

# Apply manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/postgres-stack.yaml
kubectl apply -f k8s/kafka-stack.yaml
kubectl apply -f k8s/service-stack.yaml
kubectl apply -f k8s/k3d/ingress-local.yaml

# Add /etc/hosts entries
echo "127.0.0.1 api.reportsystem.local app.reportsystem.local" | sudo tee -a /etc/hosts
```

## Service port map

| Service | Port | DB |
|---------|------|----|
| auth-service | 8081 | auth_db |
| property-service | 8082 | property_db |
| restaurant-service | 8083 | restaurant_db |
| inventory-service | 8084 | inventory_db |
| finance-service | 8085 | finance_db |
| payment-service | 8086 | payment_db |
| reporting-service | 8087 | reporting_db |
| gateway | 8080 | — |
| eureka | 8761 | — |
| nuxt-web | 3000 | — |

## Required changes before production

1. **Replace `secrets.yaml` placeholders** with real values (use Sealed Secrets, External Secrets Operator, or your vault).
2. **Externalize PostgreSQL** — the in-cluster Postgres pods are fine for dev/k3d; for prod use a managed database (RDS, Cloud SQL, or a database operator like Zalando/Percona).
3. **Externalize Kafka** — use a managed service (Confluent Cloud, MSK) or Strimzi operator.
4. **Set resource limits** — current `requests/limits` are dev-sized. Tune based on load testing.
5. **Configure HPA** — current min=1, max=2 replicas. Adjust based on actual traffic.
6. **Add monitoring** — Prometheus + Grafana, distributed tracing (Zipkin/Jaeger), log aggregation.
7. **TLS certificates** — `ingress.yaml` references `letsencrypt-prod`. Install cert-manager first.
8. **Network policies** — restrict inter-service traffic to expected paths.
9. **Pod disruption budgets** — keep at least 1 replica of each service running during node drains.
10. **Backup strategy** — schedule Postgres backups (`VolumeSnapshot` or managed snapshots).

## Image registry

The current manifests reference `reportsystem/*` images on the local Docker daemon (`imagePullPolicy: IfNotPresent`). For real deployments:

1. Push images to a registry (GHCR, ECR, GCR, Docker Hub)
2. Add `imagePullSecrets: [{name: regcred}]` to each Deployment
3. Update image tags from `latest` to immutable tags (e.g., git SHA)
