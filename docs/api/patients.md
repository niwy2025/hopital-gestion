# API Patients

Le service `patient-service` conserve le registre provincial des patients. Un
patient est rattaché à l'hôpital qui a enregistré son dossier. Les demandes
d'analyse conservent ensuite une copie de son code et de son nom pour préserver
l'historique médical.

Toutes les routes sont exposées par l'API Gateway sous `{{baseUrl}}`, soit par
défaut `http://localhost:8888`.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/patients` | Liste les dossiers patients. |
| `GET` | `/api/v1/patients/search?page=0&size=20&query=amina&hospitalId={uuid}` | Recherche paginée par code, identité ou hôpital d'enregistrement. |
| `GET` | `/api/v1/patients/passages/search?page=0&size=20&query=amina&hospitalId={uuid}&type=CONSULTATION&status=OPEN` | Registre paginé des passages visibles dans le périmètre de l’utilisateur. |
| `GET` | `/api/v1/patients/passages/{passageId}` | Consulte la fiche administrative d’un passage précis. |
| `GET` | `/api/v1/patients/{patientId}` | Consulte la fiche complète d’un patient. |
| `GET` | `/api/v1/patients/{patientId}/passages?page=0&size=20` | Consulte l’historique des passages d’un dossier. |
| `POST` | `/api/v1/patients` | Crée un dossier patient. |
| `POST` | `/api/v1/patients/{patientId}/passages` | Enregistre une arrivée dans le parcours du patient. |
| `PATCH` | `/api/v1/patients/{patientId}/passages/{passageId}/responsible-personnel` | Affecte le personnel responsable d’un passage en cours. |
| `PATCH` | `/api/v1/patients/{patientId}/passages/{passageId}/status` | Termine ou annule un passage en cours. |
| `PATCH` | `/api/v1/patients/{patientId}/status` | Active ou désactive un dossier. |

Les listes destinées à l'interface utilisent l'endpoint `search`. `page` est
indexé à partir de `0`, `size` accepte au plus `100` éléments et la réponse
contient `items`, `page`, `size`, `totalElements` et `totalPages`.

## Passage hospitalier

Chaque arrivée génère côté serveur un code unique au format
`PAS-YYYYMMDD-XXXXXXXX`. Ce code est recherché directement dans le registre et
identifie un seul passage, indépendamment du numéro de dossier du patient.

Un passage débute avec le statut `OPEN`, puis peut être `CLOSED` ou
`CANCELLED`. La fiche conserve l'hôpital, le service ou l'unité, le motif, le
patient concerné et les opérateurs d'ouverture et de clôture. Le personnel
responsable est facultatif à l'ouverture, mais il doit être actif et avoir une
affectation active dans le même hôpital. Son matricule, son nom, sa fonction et
l'opérateur qui l'a affecté sont conservés dans le passage pour l'historique.

Un passage ne peut pas être terminé sans personnel responsable. Celui-ci peut
être fourni à la création avec `responsiblePersonnelId` ou affecté ensuite :

```json
{
  "personnelId": "00000000-0000-0000-0000-000000000000"
}
```

Avant la clôture définitive, les futures opérations seront rattachées au même
passage : consultation et actes, analyses et résultats, ordonnances et
délivrances de pharmacie, lignes de facture, paiements et solde. Le module de
comptabilité ajoutera le contrôle bloquant des frais non régularisés avant une
clôture.

## Créer un patient

```json
{
  "firstName": "Amina",
  "lastName": "Kasongo",
  "middleName": "Mbuyi",
  "dateOfBirth": "1992-05-04",
  "gender": "FEMALE",
  "phoneNumber": "+243 900 000 000",
  "email": "amina.kasongo@example.cd",
  "address": "Goma",
  "nationalIdentifier": "NIN-00000001",
  "emergencyContactName": "Jean Kasongo",
  "emergencyContactRelationship": "Conjoint",
  "emergencyContactPhone": "+243 901 000 000",
  "registrationHospitalId": "00000000-0000-0000-0000-000000000000"
}
```

`gender` accepte `MALE`, `FEMALE` ou `UNSPECIFIED`. `registrationHospitalId`
est l’UUID d’un établissement actif du référentiel `organization-service`.
Le numéro de dossier est généré côté serveur : il ne doit pas être fourni par
le client. Les listes paginées ne renvoient pas les coordonnées sensibles ;
elles restent disponibles sur la fiche détaillée.

## Réponse d'erreur

Les erreurs suivent le format commun :

```json
{
  "timestamp": "2026-08-25T08:00:00Z",
  "status": 409,
  "code": "PATIENT_ALREADY_EXISTS",
  "message": "Un dossier patient similaire existe déjà.",
  "path": "/api/v1/patients"
}
```
