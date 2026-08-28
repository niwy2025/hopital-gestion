# Personnel

Le registre du personnel est géré par `personnel-service`, derrière la
passerelle via `/api/v1/personnel`.

## Enregistrement

`POST /api/v1/personnel` crée la fiche administrative d’un agent. Le
`employeeNumber` (matricule) est unique. L’affectation initiale
`hospitalId` est facultative afin de couvrir les agents du niveau provincial
ou central.

Le champ facultatif `accountId` associe un compte utilisateur existant à
l’agent. Cette référence est vérifiée par le service Accounts et ne peut être
utilisée que par une seule fiche du personnel.

```json
{
  "employeeNumber": "MED-001",
  "firstName": "Amina",
  "lastName": "Kasongo",
  "gender": "FEMALE",
  "category": "DOCTOR",
  "jobTitle": "Médecin chef",
  "hospitalId": "<uuid optionnel>",
  "accountId": "<uuid optionnel>"
}
```

Catégories actuellement prévues : `DOCTOR`, `NURSE`, `MIDWIFE`,
`PHARMACIST`, `LABORATORY_TECHNICIAN`, `ADMINISTRATIVE`, `SUPPORT` et
`OTHER`.

## Consultation et statut

- `GET /api/v1/personnel/search?page=0&size=20&query=&hospitalId=&active=true`
  fournit une liste paginée, recherchable et filtrable.
- `GET /api/v1/personnel/{personnelId}` retourne la fiche complète, y compris
  les pièces de son dossier documentaire. Les listes de recherche ne chargent
  jamais ces fichiers.
- `GET /api/v1/personnel/account/{accountId}` retourne la fiche associée à un
  compte utilisateur, ou `404` lorsque le compte n’est rattaché à aucun agent.
- `PUT /api/v1/personnel/{personnelId}` met à jour la fiche et l’association
  optionnelle avec un compte.
- `PATCH /api/v1/personnel/{personnelId}/status` avec `{ "active": false }`
  désactive un agent sans effacer son historique.

## Dossier documentaire

Chaque agent peut conserver ses pièces administratives directement dans la
base de données : photo de profil, signature, CV, pièce d’identité, diplôme,
licence professionnelle, contrat et autres documents.

`POST /api/v1/personnel/{personnelId}/documents` accepte le contenu Base64
sans préfixe `data:` :

```json
{
  "documentType": "CV",
  "fileName": "cv-amina-kasongo.pdf",
  "contentType": "application/pdf",
  "contentBase64": "<contenu-base64>"
}
```

Les formats acceptés sont PDF, Word, JPEG, PNG et WebP, avec une limite de
2 Mo par fichier. La photo, la signature et le CV remplacent la version
précédente ; les autres types peuvent avoir plusieurs pièces. Un document est
supprimé par `DELETE /api/v1/personnel/{personnelId}/documents/{documentId}`.

## Affectations

Les affectations sont historisées indépendamment de la fiche administrative.
Elles peuvent être de niveau `PROVINCIAL` (sans établissement) ou `HOSPITAL`
(avec `hospitalId`), et précisent la fonction, le service ou département, l’unité
et la période. Une seule affectation principale peut être active pour un agent.

- `GET /api/v1/personnel/{personnelId}/assignments/search?page=0&size=20&query=&status=ACTIVE`
  fournit l’historique paginé et filtrable.
- `POST /api/v1/personnel/{personnelId}/assignments` crée une affectation.
- `PATCH /api/v1/personnel/{personnelId}/assignments/{assignmentId}/close`
  clôture l’affectation avec sa date de fin.

```json
{
  "scope": "HOSPITAL",
  "hospitalId": "<uuid de l’hôpital>",
  "departmentName": "Médecine interne",
  "unitName": "Hospitalisation",
  "positionTitle": "Médecin traitant",
  "startsOn": "2026-08-28",
  "primaryAssignment": true,
  "notes": null
}
```
