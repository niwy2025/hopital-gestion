# Patient Service

Registre provincial des dossiers patients. Il conserve l'identité du patient,
ses coordonnées, son hôpital d'enregistrement et son statut. Les consultations,
hospitalisations et factures seront ajoutées dans des services métier dédiés.

## Endpoints

- `GET` / `POST` /api/v1/patients
- `PATCH` /api/v1/patients/{code}/status
