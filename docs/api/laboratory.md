# API Laboratoire

`laboratory-service` trace le parcours d’une analyse : demande, réception de
l’échantillon, saisie du résultat et validation par le biologiste. Toutes les
routes sont accessibles via Kong avec la base locale `http://localhost:8000`.

## Demandes d’analyse

- `GET /api/v1/laboratory/analysis-requests` : liste les demandes.
- `POST /api/v1/laboratory/analysis-requests` : crée une demande.

Le laboratoire exécutant est explicite : `HOSPITAL` pour un laboratoire interne
lié à un hôpital, ou `REFERENCE` pour un laboratoire provincial de référence.

```json
{
  "code": "REQ-001",
  "laboratoryType": "HOSPITAL",
  "laboratoryCode": "LAB-HGR-001",
  "patientReference": "PAT-001",
  "patientName": "Patient de démonstration",
  "analysisCode": "NFS",
  "analysisName": "Numération formule sanguine",
  "requesterName": "Dr. Mbala"
}
```

Après réception d’un échantillon, le statut `SAMPLE_RECEIVED` signifie que le
dossier patient est au laboratoire et en attente de résultat. Le futur service
Patients utilisera cet état dans le parcours du patient.

## Échantillons, résultats et validation

- `GET` / `POST /api/v1/laboratory/specimens`
- `GET` / `POST /api/v1/laboratory/analysis-results`
- `PATCH /api/v1/laboratory/analysis-results/{code}/validation`

Un résultat ne peut être saisi qu’après réception d’un échantillon et un
résultat validé ne peut pas être validé une seconde fois.
