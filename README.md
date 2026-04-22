# Store Technical Test

Full-stack con 2 microservicios Java Spring Boot, API Gateway y frontend Vue 3.

## Requisitos

- Docker >= 24 y Docker Compose v2
- Java 17+ y Gradle (solo para correr tests localmente)
- Node.js 20+ y pnpm (solo para tests del frontend)

## Levantar con Docker

```bash
cp .env.example .env
docker compose up --build
```

| Servicio | URL |
|---|---|
| Frontend (WSL2/Windows) | http://localhost:5743 |
| API Gateway | http://localhost:8080 |
| Products Service | http://localhost:8081 |
| Inventory Service | http://localhost:8082 |
| Swagger Gateway | http://localhost:8080/swagger-ui.html |
| Swagger Products | http://localhost:8081/swagger-ui.html |
| Swagger Inventory | http://localhost:8082/swagger-ui.html |

### Si Docker no puede acceder a internet durante el build

Agregar DNS explícito en `/etc/docker/daemon.json`:

```json
{
  "dns": ["8.8.8.8", "8.8.4.4"]
}
```

Luego:

```bash
sudo systemctl restart docker
docker compose up --build
```

## Tests

### Backend

```bash
cd products-service && ./gradlew test && cd ..
cd inventory-service && ./gradlew test && cd ..
```

Reporte HTML en `<servicio>/build/reports/tests/test/index.html`.

### Frontend

```bash
cd store-frontend
pnpm install
pnpm test --run
```

## Variables de entorno

Ver `.env.example`. Copiar a `.env` antes de levantar. Las variables `VITE_*` se embeben en el bundle en build time.