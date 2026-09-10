# Transferts des patients

Le circuit est géré par `patient-service`. Le patient garde son UUID, son numéro de dossier et son hôpital d’enregistrement. Un transfert interne ouvre **un nouveau passage** à l’arrivée : les actes, responsables, prescriptions et frais ne sont pas déplacés d’un établissement à l’autre.

## Parcours dans l’application

Depuis un passage en cours, utiliser **Préparer un transfert**. Le formulaire complet permet de choisir un autre hôpital actif dans le référentiel, ou de saisir une structure externe. Il demande le motif et la synthèse clinique à transmettre ; le contact, les traitements/allergies/consignes, le transport et l’urgence complètent la fiche.

| Étape | Transfert | Passage d’origine | Passage d’arrivée |
| --- | --- | --- | --- |
| Préparation | `REQUESTED` | Reste `OPEN` | Aucun |
| Départ vers un hôpital du système | `IN_TRANSIT` | `TRANSFERRED` | Aucun |
| Réception réelle par l’hôpital destinataire | `RECEIVED` | Reste `TRANSFERRED` | Nouveau passage `OPEN` |
| Départ vers une structure externe | `EXTERNAL_COMPLETED` | `TRANSFERRED` | Aucun dans la plateforme |
| Annulation avant le départ | `CANCELLED` | Inchangé | Aucun |

La réception est une confirmation humaine de l’arrivée, pas une conséquence automatique de la demande. L’équipe destinataire choisit le type de passage et le service d’accueil, puis peut affecter un responsable, effectuer le triage et continuer les soins normalement. Le personnel de départ n’est pas automatiquement affecté à l’hôpital d’arrivée.

Le registre `/transferts` propose une recherche par code, patient ou établissement, une pagination et des filtres de statut et de sens. Un administrateur peut choisir l’hôpital concerné. Le transfert est également accessible depuis le dossier patient et les passages liés.

Les listes ne renvoient pas la synthèse clinique, les traitements, les contacts ni les notes : ces informations sont chargées à l’ouverture de la fiche autorisée.

## Fiche imprimable

La fiche de liaison `/transferts/{id}` s’imprime sans le shell ni les boutons. Elle contient le code généré `TRF-…`, l’identité du patient, les établissements, la synthèse explicitement transmise, les consignes et les opérateurs/dates. Le navigateur permet l’impression papier ou l’enregistrement en PDF.

Pour une destination externe, le nom, l’adresse et le contact saisis restent dans le dossier. La plateforme enregistre le **départ**, sans prétendre connaître l’arrivée ni la prise en charge hors système. L’impression est aussi disponible avant le départ et conserve le statut « préparé » tant que celui-ci n’est pas confirmé.

## Droits et traçabilité

- Préparer, confirmer le départ ou annuler : `ADMIN`, `HOSPITAL_ADMIN` du départ, ou `DOCTOR` explicitement responsable du passage dans cet hôpital.
- Consulter : `ADMIN`, ou `HOSPITAL_ADMIN`, `DOCTOR`, `NURSE`, `RECEPTIONIST` d’un des deux hôpitaux concernés.
- Réceptionner : `ADMIN`, ou l’un de ces rôles dans l’hôpital destinataire uniquement.
- Les droits sont recalculés côté backend à partir du compte authentifié. Les indicateurs `canDispatch`, `canCancel` et `canReceive` ne remplacent pas ces contrôles.
- Avant réception, l’équipe destinataire accède à la fiche transmise, mais n’acquiert pas pour autant l’accès administratif au patient. Après réception, son passage lui donne accès au patient. Les détails des passages restent limités à leur propre hôpital ; les anciennes données cliniques ne sont pas intégralement ouvertes par le transfert.
- Les UUID opérateurs, identifiants de connexion et horodatages sont enregistrés côté serveur ; aucun opérateur n’est fourni dans le payload.
- Un seul transfert non annulé est autorisé par passage. Les départs et réceptions sont verrouillés en transaction ; répéter une réception ne crée pas un second passage.
- Un passage transféré ne peut pas être rouvert ou transféré par la route générique de changement de statut.

Les frais, soldes et prescriptions restent rattachés au passage d’origine. Un transfert n’est ni un encaissement ni une clôture comptable ; il ne solde rien et ne crée aucune écriture de paiement. Les prescriptions déjà émises peuvent rester à régulariser dans leur circuit propre. La fiche ne remplace pas la coordination entre les équipes ni l’organisation du transport.

## API

Toutes les routes publiques nécessitent un Bearer valide. Dans Next, les appels passent par les Server Actions BFF avec la session serveur, sans exposer les tokens au navigateur.

| Méthode | Route relative à `/api/v1/patients` | Usage |
| --- | --- | --- |
| GET | `/transfers/search` | Liste paginée dans le périmètre autorisé |
| GET | `/transfers/{id}` | Fiche et actions autorisées |
| POST | `/passages/{passageId}/transfers` | Préparer, réponse `201` |
| POST | `/transfers/{id}/dispatch` | Confirmer le départ |
| POST | `/transfers/{id}/receive` | Réceptionner et créer le passage d’arrivée |
| POST | `/transfers/{id}/cancel` | Annuler avant départ |

Filtres : `page` (base 0), `size` (1–100), `query`, `status`, `direction` (`INCOMING`/`OUTGOING`), `hospitalId` (administrateur uniquement), `patientId`, `passageId`. Le sens s’applique à l’hôpital du compte, ou à celui sélectionné par l’administrateur. Aucun filtre client ne peut élargir le périmètre hospitalier du compte.

Préparation interne :

```json
{
  "destinationHospitalId": "00000000-0000-0000-0000-000000000002",
  "reason": "Poursuite de la prise en charge",
  "clinicalSummary": "Synthèse à rédiger par le médecin responsable.",
  "treatmentAndInstructions": "Traitements, allergies et consignes à compléter.",
  "transportDetails": "Transport et accompagnement convenus entre les équipes.",
  "urgent": false
}
```

Préparation externe : remplacer `destinationHospitalId` par `null`, renseigner `externalFacilityName` (obligatoire), et éventuellement `externalFacilityAddress` et `destinationContact`. Les deux types de destination sont exclusifs.

Réception :

```json
{
  "type": "CONSULTATION",
  "serviceName": "Consultations externes",
  "receptionNote": "Arrivée constatée par l’équipe d’accueil."
}
```

Types proposés dans l’interface : `CONSULTATION`, `EMERGENCY`, `HOSPITALIZATION`, `OTHER`. Annulation : `{"reason":"Motif obligatoire"}`. Le code et les UUID sont générés côté backend. Les erreurs de périmètre renvoient `403`, les transitions interdites `409`, les champs invalides `400`.

Des exemples figurent dans la collection Postman patient et la collection globale. Variables : `passage_id`, `transfer_id`, `destination_hospital_id`, `accessToken`, `baseUrl`. Pour tester la réception, utiliser le token d’un compte de l’hôpital destinataire.

## Déploiement et vérification

Déployer `organization-service` (nom d’hôpital dans la référence interne), `patient-service` et le frontend ensemble. Flyway applique `V20_add_patient_transfers.sql` automatiquement ; aucune remise à zéro des données existantes n’est nécessaire.

```bash
docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml up -d --build organization-service patient-service
```

Tests métier : `mvn -pl services/patient,services/organization test`.

Le test `PatientTransferPersistenceTest` est activable avec `TRANSFER_POSTGRES_TEST=true` et les variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, sur une base PostgreSQL **de test vide**. Il applique les migrations, vérifie les contraintes et le parcours interne, et contrôle les listes visibles aux deux hôpitaux. Ne pas le lancer sur une base métier.
