# API Laboratoire

`laboratory-service` trace le parcours d’une analyse : demande, réception de
l’échantillon, saisie du résultat et validation par le biologiste. Toutes les
routes sont accessibles via l'API Gateway avec la base locale
`http://localhost:8888`.

## Demandes d’analyse

- `GET /api/v1/laboratory/analysis-requests` : liste les demandes.
- `GET /api/v1/laboratory/analysis-requests/search?page=0&size=20&query=REQ-001` : recherche paginée.
- `GET /api/v1/laboratory/analysis-requests/{requestCode}` : détail d’une demande, de ses échantillons et de son résultat.
- `POST /api/v1/laboratory/analysis-requests` : crée une demande.

Le laboratoire exécutant est explicite : `HOSPITAL` pour un laboratoire interne
lié à un hôpital, ou `REFERENCE` pour un laboratoire provincial de référence.

```json
{
  "laboratoryType": "HOSPITAL",
  "laboratoryCode": "LAB-HGR-001",
  "patientReference": "PAT-001",
  "patientName": "Patient de démonstration",
  "analysisName": "Numération formule sanguine",
  "requesterName": "Dr. Mbala"
}
```

Les identifiants de traçabilité sont générés par le service : `LAB-…` pour la
demande, `ANL-…` pour l’analyse, `ECH-…` pour chaque échantillon et `RES-…`
pour le résultat. Ils ne sont jamais fournis par le navigateur.

Après réception d’un échantillon, le statut `SAMPLE_RECEIVED` signifie que le
dossier patient est au laboratoire et en attente de résultat. Le futur service
Patients utilisera cet état dans le parcours du patient.

## Analyses depuis un passage patient

Une demande initiée dans le dossier d’un passage est liée à son identifiant
technique et non à une simple saisie du patient. Elle est exécutée uniquement
par un laboratoire interne actif de l’hôpital de ce passage : elle ne bascule
jamais automatiquement vers un laboratoire provincial de référence.

- `GET /api/v1/laboratory/patient-passages/{passageId}/hospital-laboratories`
  retourne les laboratoires internes actifs disponibles.
- `GET /api/v1/laboratory/patient-passages/{passageId}/analysis-requests/search?page=0&size=20&query=&status=`
  retourne le suivi paginé de chaque demande, de ses échantillons et de son
  résultat.
- `POST /api/v1/laboratory/patient-passages/{passageId}/analysis-requests`
  crée une demande avec les codes de demande et d’analyse générés par le service.
- `POST /api/v1/laboratory/patient-passages/{passageId}/analysis-requests/{requestCode}/specimens`
  réceptionne un échantillon de cette demande.
- `POST /api/v1/laboratory/patient-passages/{passageId}/analysis-requests/{requestCode}/results`
  saisit le résultat.
- `PATCH /api/v1/laboratory/patient-passages/{passageId}/analysis-requests/{requestCode}/results/{resultCode}/validation`
  valide ce résultat.

Le passage doit rester `OPEN` pendant les opérations de laboratoire. Le patient,
l’hôpital et le demandeur sont récupérés côté services ; le navigateur ne peut
pas les substituer dans la requête.

## Échantillons, résultats et validation

- `GET` / `POST /api/v1/laboratory/specimens`
- `GET /api/v1/laboratory/specimens/{specimenCode}` : détail d’un échantillon, de sa demande et de la suite de son traitement.
- `GET` / `POST /api/v1/laboratory/analysis-results`
- `PATCH /api/v1/laboratory/analysis-results/{code}/validation`

Les endpoints `GET /api/v1/laboratory/specimens/search` et
`GET /api/v1/laboratory/analysis-results/search` prennent également `page`,
`size` et `query`. Ils renvoient `items`, `page`, `size`, `totalElements` et
`totalPages`; `page` commence à `0` et `size` est limité à `100`.

Un résultat ne peut être saisi qu’après réception d’un échantillon et un
résultat validé ne peut pas être validé une seconde fois.

Exemple de réception d’échantillon :

```json
{
  "analysisRequestCode": "LAB-D9D23E25F084",
  "specimenType": "BLOOD",
  "collectedAt": "2026-09-01T14:30:00Z"
}
```

Exemple de saisie de résultat :

```json
{
  "analysisRequestCode": "LAB-D9D23E25F084",
  "resultValue": "12.4",
  "unit": "g/dL",
  "referenceRange": "12 - 16",
  "comment": null
}
```
