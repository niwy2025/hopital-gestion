# Kong gateway configuration

This project starts Kong with a PostgreSQL-backed configuration so routes can be
managed through the Kong Admin API from the Docker network.

When hospital microservices are created, register each service with commands like:

```bash
curl -i -X POST http://localhost:8001/services \
  --data name=patient-service \
  --data url=http://patient-service:8081

curl -i -X POST http://localhost:8001/services/patient-service/routes \
  --data name=patient-routes \
  --data 'paths[]=/api/v1/patients' \
  --data strip_path=false
```

Kong remains internal. Public API access is published by the system Nginx,
which forwards the API domain to `api-gateway:8088` through the host port
`127.0.0.1:8888`. Do not expose business services directly outside Docker
Compose.
