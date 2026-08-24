# Référentiel provincial

`organization-service` porte le référentiel institutionnel du système : une
province contient des zones de santé et une zone de santé contient des
hôpitaux ou centres de soins. Toutes les routes sont accessibles via Kong avec
la base locale `http://localhost:8000`.

> La restriction aux administrateurs provinciaux et centraux sera appliquée
> avec les rôles Keycloak lors du lot sécurité. Les routes actuelles ne doivent
> pas être exposées sans cette protection dans un environnement de production.

## Provinces

- `GET /api/v1/organizations/provinces` : liste les provinces.
- `POST /api/v1/organizations/provinces` : crée une province.

```json
{
  "code": "KIN",
  "name": "Kinshasa"
}
```

## Zones de santé

- `GET /api/v1/organizations/health-zones` : liste les zones de santé.
- `POST /api/v1/organizations/health-zones` : crée une zone rattachée à une
  province existante.

```json
{
  "code": "KINSENSO",
  "name": "Kinsenso",
  "provinceCode": "KIN"
}
```

## Hôpitaux et centres

- `GET /api/v1/organizations/hospitals` : liste les structures.
- `POST /api/v1/organizations/hospitals` : crée une structure dans une zone de
  santé existante.

```json
{
  "code": "HGR-KIN-001",
  "name": "Hôpital général de référence de Kinsenso",
  "type": "GENERAL_REFERENCE",
  "healthZoneCode": "KINSENSO",
  "address": "Avenue de la Santé",
  "phoneNumber": "+243810000000"
}
```

Les valeurs possibles pour `type` sont `PROVINCIAL`, `GENERAL_REFERENCE`,
`SPECIALIZED` et `HEALTH_CENTER`.

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
