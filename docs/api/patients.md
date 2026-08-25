# API Patients

Le service `patient-service` conserve le registre provincial des patients. Un
patient est rattaché à l'hôpital qui a enregistré son dossier. Les demandes
d'analyse conservent ensuite une copie de son code et de son nom pour préserver
l'historique médical.

Toutes les routes sont exposées par Kong sous `{{baseUrl}}`, soit par défaut
`http://localhost:8000`.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/patients` | Liste les dossiers patients. |
| `POST` | `/api/v1/patients` | Crée un dossier patient. |
| `PATCH` | `/api/v1/patients/{code}/status` | Active ou désactive un dossier. |

## Créer un patient

```json
{
  "code": "PAT-0001",
  "firstName": "Amina",
  "lastName": "Kasongo",
  "dateOfBirth": "1992-05-04",
  "gender": "FEMALE",
  "phoneNumber": "+243 900 000 000",
  "address": "Goma",
  "registrationHospitalCode": "HGR-GOMA-001"
}
```

`gender` accepte `MALE`, `FEMALE` ou `UNSPECIFIED`. Le code de l'hôpital est
une référence au référentiel `organization-service`; il doit donc être choisi
parmi les établissements existants dans l'interface.

## Réponse d'erreur

Les erreurs suivent le format commun :

```json
{
  "timestamp": "2026-08-25T08:00:00Z",
  "status": 409,
  "code": "PATIENT_ALREADY_EXISTS",
  "message": "Un patient avec le code PAT-0001 existe déjà.",
  "path": "/api/v1/patients"
}
```
