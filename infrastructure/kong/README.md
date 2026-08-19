# Kong gateway configuration

This project starts Kong with a PostgreSQL-backed configuration so routes can be
managed through the Kong Admin API on `http://localhost:8001`.

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

Keep public access through Kong on `http://localhost:8000`; avoid exposing
business services directly outside Docker Compose unless needed for debugging.
