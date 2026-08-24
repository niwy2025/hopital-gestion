# Organization Service

Service de référentiel territorial et institutionnel de la plateforme
provinciale. Il permet à l'administration centrale de créer et de consulter :

- les provinces ;
- les zones de santé ;
- les hôpitaux publics et centres de santé.

Les services métier futurs (personnel, patients, pharmacie et laboratoire)
utiliseront le code de l'hôpital comme périmètre de travail et d'autorisation.

## Endpoints initiaux

- `GET` / `POST /api/v1/organizations/provinces`
- `GET` / `POST /api/v1/organizations/health-zones`
- `GET` / `POST /api/v1/organizations/hospitals`
