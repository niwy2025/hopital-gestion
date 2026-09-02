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
| `GET` | `/api/v1/patients/passages/search?page=0&size=20&query=amina&hospitalId={uuid}&type=CONSULTATION&status=OPEN&assignedToMe=true` | Registre paginé des passages visibles dans le périmètre de l’utilisateur. Avec `assignedToMe=true`, il ne contient que les passages confiés au personnel connecté. |
| `GET` | `/api/v1/patients/passages/{passageId}` | Consulte la fiche administrative d’un passage précis. |
| `GET` | `/api/v1/patients/{patientId}` | Consulte la fiche complète d’un patient. |
| `GET` | `/api/v1/patients/{patientId}/passages?page=0&size=20` | Consulte l’historique des passages d’un dossier. |
| `POST` | `/api/v1/patients` | Crée un dossier patient. |
| `POST` | `/api/v1/patients/{patientId}/passages` | Enregistre une arrivée dans le parcours du patient. |
| `PATCH` | `/api/v1/patients/{patientId}/passages/{passageId}/responsible-personnel` | Affecte le personnel responsable d’un passage en cours. |
| `GET` | `/api/v1/patients/{patientId}/passages/{passageId}/clinical-entries/search?page=0&size=20&query=toux&entryType=CLINICAL_EVOLUTION&orientation=LABORATORY` | Consulte le journal clinique paginé d’un passage. |
| `POST` | `/api/v1/patients/{patientId}/passages/{passageId}/clinical-entries` | Ajoute une évolution clinique à un passage en cours. |
| `GET` | `/api/v1/patients/{patientId}/passages/{passageId}/prescriptions/search?page=0&size=20&query=amoxicilline&source=MEDICAL` | Consulte les ordonnances paginées d’un passage. |
| `POST` | `/api/v1/patients/{patientId}/passages/{passageId}/prescriptions` | Ajoute une ordonnance médicale ou enregistre une ordonnance externe. |
| `GET` | `/api/v1/patients/pharmacy/prescriptions/search?page=0&size=20&query=amoxicilline&status=PENDING_DISPENSING` | File paginée de la pharmacie, limitée à l’établissement du pharmacien. |
| `GET` | `/api/v1/patients/pharmacy/prescriptions/{prescriptionId}` | Consulte l’ordonnance, ses médicaments et ses délivrances. |
| `POST` | `/api/v1/patients/pharmacy/prescriptions/{prescriptionId}/dispenses` | Enregistre une délivrance complète ou partielle. |
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

Le personnel responsable peut terminer ou annuler son propre passage après
connexion. Un autre soignant du même hôpital conserve la consultation seule ;
un administrateur peut gérer tous les passages. La fiche détaillée retourne le
booléen `canManageStatus`, calculé côté serveur pour l'utilisateur connecté ;
la même règle est à nouveau vérifiée lors de la modification du statut.

## Journal clinique

Un passage possède un journal de plusieurs évolutions cliniques. Chaque note
contient les constatations, le diagnostic ou l’hypothèse, la conduite à tenir,
l’orientation et une date de contrôle éventuelle. Elle est ajoutée à
l’historique, sans jamais écraser les notes précédentes.

L’ajout est réservé au personnel responsable du passage, ou à un administrateur,
tant que le passage est `OPEN`. Chaque évolution est datée et signée ; elle est
aussi inscrite dans la traçabilité du dossier patient. Le journal est consultable
par les utilisateurs ayant accès au passage, avec pagination, recherche, filtre
par nature de note et filtre par orientation.

```json
{
  "entryType": "CLINICAL_EVOLUTION",
  "clinicalFindings": "Toux persistante, température à 38,5 °C.",
  "diagnosis": "Infection respiratoire à confirmer",
  "carePlan": "Hydratation, surveillance et bilan complémentaire.",
  "orientation": "LABORATORY",
  "followUpOn": "2026-09-04"
}
```

## Ordonnances et pharmacie

Une ordonnance est toujours liée à un passage `OPEN`. Son code `ORD-YYYYMMDD-XXXXXXXX`
est généré par le serveur ; l’interface ne le demande jamais à l’utilisateur.

Le personnel responsable du passage — en pratique le médecin en charge — ou un
administrateur peut créer une ordonnance `MEDICAL`. Celle-ci est signée par
l’auteur connecté. Le rôle `PHARMACIST` peut enregistrer une ordonnance
`EXTERNAL_PAPER` présentée par le patient, en indiquant obligatoirement le nom
du prescripteur externe. Ces deux origines restent distinctes pour la
traçabilité et ne permettent pas d’attribuer au pharmacien une prescription
médicale qu’il n’a pas établie.

```json
{
  "source": "MEDICAL",
  "notes": "À prendre après le repas.",
  "items": [
    {
      "medicineName": "Amoxicilline",
      "dosage": "500 mg",
      "administrationRoute": "Voie orale",
      "frequency": "3 fois par jour",
      "duration": "7 jours",
      "quantity": "21 gélules",
      "instructions": "Terminer le traitement."
    }
  ]
}
```

Chaque ordonnance débute à l’état `PENDING_DISPENSING`. Le catalogue et le
stock seront ajoutés au module Pharmacie sans modifier la prescription ni ses
délivrances déjà tracées.

### Délivrance par la pharmacie

La pharmacie dispose d’une file séparée des prescriptions de passage. Elle ne
voit que les ordonnances de son hôpital ; l’administrateur provincial peut voir
la file complète. Chaque remise génère un reçu `DSP-YYYYMMDD-XXXXXXXX`, signé
par le pharmacien, avec les médicaments et quantités effectivement remis.

Une remise peut être `PARTIAL` ou `COMPLETE`. Une remise complète doit couvrir
toutes les lignes restant à remettre ; elle fait passer l’ordonnance à
`DISPENSED`. Une remise partielle laisse l’ordonnance à `PARTIALLY_DISPENSED`
afin de permettre un complément ultérieur. Une ligne déjà remise est verrouillée
pour éviter une double délivrance, et une ordonnance annulée ou déjà délivrée
ne peut pas être remise une seconde fois.

```json
{
  "complete": false,
  "notes": "Le sirop est indisponible ; le patient reviendra demain.",
  "items": [
    {
      "prescriptionItemId": "3f03d0a3-10a1-4ed6-8a7e-26f9b51d7a40",
      "dispensedQuantity": "21 gélules"
    }
  ]
}
```

La clôture d’un passage est désormais refusée lorsqu’une de ses ordonnances est
encore `PENDING_DISPENSING` ou `PARTIALLY_DISPENSED`. Cela garde le parcours,
la délivrance et l’audit cohérents avant l’ajout de la facturation.

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
