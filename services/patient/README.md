# Patient Service

Registre provincial des dossiers patients. Il génère un numéro de dossier unique
et conserve l'identité, les coordonnées, le contact d'urgence et l'hôpital
d'enregistrement du patient. Les consultations, hospitalisations et factures
seront ajoutées dans des services métier dédiés.

## Endpoints

- `GET` / `POST` /api/v1/patients
- `GET /api/v1/patients/search?page=0&size=20&query=...&hospitalId=...&active=true`
- `GET /api/v1/patients/{id}`
- `PATCH` /api/v1/patients/{id}/status
