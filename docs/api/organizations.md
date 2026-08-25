# Référentiel provincial

`organization-service` porte le référentiel institutionnel du système : une
province contient des zones de santé, une zone de santé contient des aires de
santé et une aire de santé contient des hôpitaux ou centres de soins. Toutes les routes sont accessibles via l'API
Gateway avec la base locale `http://localhost:8888`.

> La restriction aux administrateurs provinciaux et centraux sera appliquée
> avec les rôles Keycloak lors du lot sécurité. Les routes actuelles ne doivent
> pas être exposées sans cette protection dans un environnement de production.

## Provinces

- `GET /api/v1/organizations/provinces` : liste les provinces.
- `GET /api/v1/organizations/provinces/search` : liste paginée avec recherche
  par code ou nom.
- `POST /api/v1/organizations/provinces` : crée une province.

```json
{
  "code": "KIN",
  "name": "Kinshasa"
}
```

## Zones de santé

- `GET /api/v1/organizations/health-zones` : liste les zones de santé.
- `GET /api/v1/organizations/health-zones/search` : liste paginée, avec
  recherche par code ou nom et filtre de province.
- `POST /api/v1/organizations/health-zones` : crée une zone rattachée à une
  province existante.

Exemple :

`GET /api/v1/organizations/health-zones/search?page=0&size=20&query=BOKO&provinceCode=KONGO-CENTRAL`

La réponse contient `items`, `page`, `size`, `totalElements` et `totalPages`.

```json
{
  "code": "KINSENSO",
  "name": "Kinsenso",
  "provinceCode": "KIN"
}
```

## Hôpitaux et centres

- `GET /api/v1/organizations/hospitals` : liste les structures.
- `GET /api/v1/organizations/hospitals/search` : liste paginée avec recherche
  et filtre de province.
- `POST /api/v1/organizations/hospitals` : crée une structure dans une zone de
  santé. L'aire est obligatoire pour un centre de santé et facultative pour un
  hôpital de référence ; lorsqu'elle est indiquée, elle doit appartenir à la
  zone sélectionnée.

```json
{
  "code": "HGR-KIN-001",
  "name": "Hôpital général de référence de Kinsenso",
  "type": "GENERAL_REFERENCE",
  "healthZoneCode": "KINSENSO",
  "healthAreaCode": "KINSENSO-CENTRE",
  "address": "Avenue de la Santé",
  "phoneNumber": "+243810000000"
}
```

Les valeurs possibles pour `type` sont `PROVINCIAL`, `GENERAL_REFERENCE`,
`SPECIALIZED` et `HEALTH_CENTER`. Le champ `healthAreaCode` est obligatoire
pour le type `HEALTH_CENTER`.

## Aires de santé

- `GET /api/v1/organizations/health-areas` : liste les aires de santé.
- `GET /api/v1/organizations/health-areas/search` : liste paginée avec
  recherche et filtre de province.
- `POST /api/v1/organizations/health-areas` : crée une aire dans une zone de
  santé existante.

```json
{
  "code": "KINSENSO-CENTRE",
  "name": "Kinsenso Centre",
  "healthZoneCode": "KINSENSO"
}
```

## Laboratoires internes

Un laboratoire interne appartient obligatoirement à un hôpital. Il est utilisé
pour les analyses réalisées directement pendant le parcours du patient dans cet
hôpital, contrairement à un laboratoire provincial de référence.

- `GET /api/v1/organizations/hospital-laboratories` : liste les laboratoires
  internes.
- `GET /api/v1/organizations/hospital-laboratories/search` : liste paginée
  avec recherche par laboratoire ou établissement.
- `POST /api/v1/organizations/hospital-laboratories` : crée un laboratoire
  interne dans un hôpital existant.
- `PATCH /api/v1/organizations/hospital-laboratories/{code}/status` : active
  ou désactive le laboratoire.

```json
{
  "code": "LAB-HGR-001",
  "name": "Laboratoire interne de l’HGR Kinsenso",
  "hospitalCode": "HGR-KIN-001",
  "location": "Bâtiment principal, niveau 1",
  "phoneNumber": "+243810000002"
}
```

## Laboratoires de référence et structures

- `GET /api/v1/organizations/reference-laboratories/search` : liste paginée
  avec recherche et filtre de province.
- `GET /api/v1/organizations/laboratory-structures/search` : liste paginée
  avec recherche par structure ou laboratoire.

Tous les endpoints `search` acceptent `page` et `size` (1 à 100) et renvoient
`items`, `page`, `size`, `totalElements` et `totalPages`.

Les erreurs métiers respectent le format commun suivant :

```json
{
  "timestamp": "2026-08-24T10:00:00Z",
  "status": 409,
  "code": "ORGANIZATION_ALREADY_EXISTS",
  "message": "Une province possède déjà le code KIN.",
  "path": "/api/v1/organizations/provinces"
}
```
