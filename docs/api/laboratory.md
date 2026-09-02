# API Laboratoire

`laboratory-service` trace le parcours d’une analyse : demande, prélèvement,
transport éventuel, réception, saisie du résultat et validation par le
biologiste. Toutes les routes sont accessibles via l'API Gateway avec la base locale
`http://localhost:8888`.

## Demandes d’analyse

- `GET /api/v1/laboratory/analysis-requests` : liste les demandes.
- `GET /api/v1/laboratory/analysis-requests/search?page=0&size=20&query=REQ-001` : recherche paginée.
- `GET /api/v1/laboratory/analysis-requests/{requestCode}` : détail d’une demande, de ses échantillons et de son résultat.
- `POST /api/v1/laboratory/analysis-requests` : point d’entrée administratif
  exceptionnel. Une demande clinique doit être créée depuis un passage patient.

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
dossier du passage est au laboratoire et en attente de résultat. Cet état est
déjà visible dans la chronologie du passage patient.

## Analyses depuis un passage patient

Une demande initiée dans le dossier d’un passage est liée à son identifiant
technique et non à une simple saisie du patient. Le patient, l’hôpital d’origine
et le demandeur sont récupérés côté services : le navigateur ne peut pas les
substituer.

Deux parcours sont disponibles au moment de créer la demande :

- `HOSPITAL` : analyse réalisée par un laboratoire interne actif de l’hôpital ;
- `REFERENCE` : analyse envoyée vers un laboratoire de référence actif de la
  même province.

- `GET /api/v1/laboratory/patient-passages/{passageId}/hospital-laboratories`
  retourne les laboratoires internes actifs disponibles.
- `GET /api/v1/laboratory/patient-passages/{passageId}/reference-laboratories`
  retourne les laboratoires de référence actifs de la province de l’hôpital.
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

```json
{
  "laboratoryType": "REFERENCE",
  "laboratoryCode": "LRP-KC",
  "analysisName": "Culture bactérienne",
  "priority": "URGENT",
  "clinicalIndication": "Fièvre persistante malgré le traitement initial"
}
```

Le passage doit rester `OPEN` pour créer une demande, prélever ou expédier.
Le laboratoire de référence peut ensuite réceptionner, saisir et valider le
résultat même si le passage a été clôturé entre-temps.

### Référencement vers un laboratoire de référence

Le parcours est strictement ordonné et chaque transition est ajoutée à la
chronologie de la demande :

1. l’hôpital crée la demande (`REQUESTED`) ;
2. il prélève l’échantillon (`SAMPLE_COLLECTED`) ;
3. il le remet au transport (`SAMPLE_IN_TRANSIT`) ;
4. le laboratoire de référence l’accepte (`SAMPLE_RECEIVED`) ou le refuse
   (`RECOLLECTION_REQUIRED`) ;
5. le laboratoire saisit le résultat (`RESULT_ENTERED`) puis le valide
   (`VALIDATED`).

Les opérations de prélèvement et d’expédition restent du côté de l’hôpital :

- `POST /api/v1/laboratory/patient-passages/{passageId}/analysis-requests/{requestCode}/reference-specimens`
  crée le prélèvement et renvoie le code `ECH-…` à coller sur le contenant et
  le bordereau.
- `POST /api/v1/laboratory/patient-passages/{passageId}/analysis-requests/{requestCode}/reference-specimens/{specimenCode}/dispatch`
  trace la date, le transporteur et les observations d’expédition.

La réception est réalisée par le laboratoire de référence :

- `GET /api/v1/laboratory/analysis-requests/reference-receptions/search?page=0&size=20&query=`
  expose la file paginée des seuls échantillons `REFERENCE` en transit destinés
  au laboratoire actif de l’utilisateur. Le périmètre de l’hôpital d’origine
  ne donne jamais accès à cette file ;
- `POST /api/v1/laboratory/analysis-requests/{requestCode}/reference-specimens/{specimenCode}/receive`
  accepte le contenant et ses conditions de réception ;
- `POST /api/v1/laboratory/analysis-requests/{requestCode}/reference-specimens/{specimenCode}/reject`
  refuse le contenant avec un motif obligatoire afin que l’hôpital effectue un
  nouveau prélèvement.

Une demande n’accepte qu’un échantillon à la fois. Après un refus explicite,
un nouveau prélèvement est autorisé ; une même réception ou expédition ne peut
pas être rejouée.

Exemple de réception au laboratoire de référence :

```json
{
  "receivedAt": "2026-09-02T09:15:00Z",
  "receptionCondition": "Contenant intègre et température conforme"
}
```

Le code de demande et le code d’échantillon sont portés par l’URL et affichés
sur le bordereau ; ils ne sont donc jamais saisis à nouveau dans le formulaire.

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

## Périmètre des données

Le backend applique le périmètre actif du compte, indépendamment de l’interface :

- le personnel d’un hôpital crée, prélève et expédie seulement depuis son
  propre hôpital ;
- le personnel affecté à un laboratoire interne traite seulement ce laboratoire ;
- le personnel affecté à un laboratoire de référence traite seulement les
  demandes dont ce laboratoire est la destination ;
- les utilisateurs provinciaux et les administrateurs disposent d’une vue
  transversale selon leurs permissions.

L’affectation `REFERENCE_LABORATORY` est enregistrée par `personnel-service` et
validée contre le référentiel des laboratoires de référence actifs.

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
