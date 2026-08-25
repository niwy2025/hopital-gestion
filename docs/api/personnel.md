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
- `GET /api/v1/personnel/{personnelId}` retourne une fiche.
- `PUT /api/v1/personnel/{personnelId}` met à jour la fiche et l’association
  optionnelle avec un compte.
- `PATCH /api/v1/personnel/{personnelId}/status` avec `{ "active": false }`
  désactive un agent sans effacer son historique.
