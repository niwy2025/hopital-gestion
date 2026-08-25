# Organization Service

Service de référentiel territorial et institutionnel de la plateforme
provinciale. Il permet à l'administration centrale de créer et de consulter :

- les provinces ;
- les zones de santé ;
- les aires de santé ;
- les hôpitaux publics et centres de santé rattachés à une aire de santé.
- les laboratoires internes rattachés à un hôpital ;
- les laboratoires provinciaux de référence ;
- les services, unités et départements de ces laboratoires.

Les services métier futurs (personnel, patients, pharmacie et laboratoire)
utiliseront le code de l'hôpital comme périmètre de travail et d'autorisation.

## Endpoints initiaux

- `GET` / `POST /api/v1/organizations/provinces`
- `GET /api/v1/organizations/provinces/search?page=0&size=20&query=`
- `GET` / `POST /api/v1/organizations/health-zones`
- `GET /api/v1/organizations/health-zones/search?page=0&size=20&query=&provinceCode=`
- `GET` / `POST /api/v1/organizations/health-areas`
- `GET /api/v1/organizations/health-areas/search?page=0&size=20&query=&provinceCode=`
- `GET` / `POST /api/v1/organizations/hospitals`
- `GET /api/v1/organizations/hospitals/search?page=0&size=20&query=&provinceCode=`
- `GET` / `POST /api/v1/organizations/hospital-laboratories`
- `GET /api/v1/organizations/hospital-laboratories/search?page=0&size=20&query=`
- `GET` / `POST /api/v1/organizations/reference-laboratories`
- `GET /api/v1/organizations/reference-laboratories/search?page=0&size=20&query=&provinceCode=`
- `GET` / `POST /api/v1/organizations/laboratory-structures`
- `GET /api/v1/organizations/laboratory-structures/search?page=0&size=20&query=`

Chaque ressource peut être activée ou désactivée par `PATCH`
`/api/v1/organizations/<ressource>/{code}/status` avec le corps
`{ "active": true }`.
