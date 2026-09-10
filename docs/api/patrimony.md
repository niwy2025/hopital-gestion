# Patrimoine et intendance

Le patrimoine physique est géré dans **organization-service** (base
`hospital_organization`). Le registre comptable et les amortissements sont dans
**accounting-service**. Aucun nouveau conteneur ni port public n’est nécessaire.
Les médicaments et consommables restent dans leur gestion de stock.

## Mise en service

Reconstruire les services modifiés, puis appliquer les migrations au démarrage.
Ne pas effacer les volumes ni réparer les anciennes migrations :

```bash
docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml up -d --build account-service auth-service organization-service accounting-service
docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml run --rm keycloak-init
```

En local, omettre `--env-file .env -f docker-compose.yml -f docker-compose.prod.yml`.
Le script Keycloak ajoute les nouveaux rôles au realm existant sans réimporter
ni écraser les utilisateurs. Reconnecter ensuite les utilisateurs concernés.

Dans le dépôt frontend, reconstruire `hopital-front` avec son Compose de production.
Le nouveau module apparaît dans **Modules → Patrimoine et intendance**.

Migrations ajoutées : account V15, organization V5, accounting V3.

## Rôles et périmètre

| Rôle | Droits |
|---|---|
| ADMIN | Administration provinciale, choix explicite de l’hôpital |
| HOSPITAL_ADMIN | Biens, locaux, maintenance, lits et validation des sorties de son hôpital |
| INTENDANT | Inventaire, réception, déplacements, prêts, documents et maintenance |
| MAINTENANCE_TECHNICIAN | Consultation, interventions et documents |
| DOCTOR, NURSE, RECEPTIONIST | Disponibilité, réservation, occupation et libération des lits |
| HOSPITAL_ACCOUNTANT, FINANCE_MANAGER | Consultation du patrimoine, qualification comptable, préparation des amortissements |
| FINANCE_AUDITOR | Consultation du patrimoine et du registre comptable |

Pour un intendant : créer son compte, attribuer le rôle **INTENDANT**, le lier
à son dossier personnel et à une **affectation principale active dans l’hôpital**.
Un hôpital simplement fourni dans le formulaire ne remplace pas le contrôle
du périmètre actif côté serveur. Une affectation limitée à un laboratoire ne
donne pas accès à tout le patrimoine hospitalier.

Les identités des patients ne sont pas retournées aux intendants dans la liste
des lits. Aucune permission de soins, de délivrance ou d’encaissement ne leur est
ajoutée. Les boutons et le shell suivent les droits ; les API les vérifient aussi.

## Parcours

1. Créer le bâtiment, éventuellement ses étages, puis ses salles.
2. Enregistrer chaque bien suivi individuellement : UUID et numéro `INV-…`
   générés, catégorie, désignation, marque, modèle, emplacement, propriétaire,
   fournisseur/donateur, dates, coût connu, devise CDF/USD, garantie, entretien.
   Le numéro de série **du fabricant** reste saisissable : il n’est pas généré
   par l’application.
3. Affecter un personnel actif de l’hôpital comme responsable du bien.
4. Ajouter photo (import/caméra et recadrage), notice, facture, garantie, rapport.
   PDF/JPEG/PNG/WebP, 3 Mo par fichier, signature du format vérifiée ; contenu
   absent des listes. La caméra nécessite HTTPS ou localhost.
5. Imprimer l’étiquette avec QR contenant uniquement le numéro d’inventaire.
6. Enregistrer mouvements internes, prêt/retour, présence/absence à un contrôle,
   panne ou maintenance. L’historique conserve l’opérateur et la date.
7. Demander une réforme/perte définitive. Un **autre** administrateur doit
   approuver. Un constat d’absence ne supprime jamais automatiquement le bien.

Les salles sont des emplacements, pas automatiquement des immobilisations
comptables. Un bâtiment peut avoir sa propre fiche de bien si nécessaire.
Les 16 catégories couvrent notamment lits/berceaux, matelas, fauteuils roulants,
brancards, mobilier, consultation/triage, urgences, maternité, laboratoire,
chaîne du froid, hygiène, informatique, eau/énergie/sécurité, transport et
équipements spécialisés. La catégorie « Autres biens durables » reste disponible.

### Lits

Un lit doit être en état de service, propre et rattaché à une salle active.
La réservation/occupation porte sur un **passage OPEN du même hôpital**.
Contraintes SQL uniques : un occupant actif par lit, un lit actif par passage.
Le verrouillage protège les affectations concurrentes.

Après libération, le nettoyage doit être confirmé par l’intendance. Impossible
de déplacer, prêter ou mettre en maintenance un lit occupé. Une panne peut être
signalée sans effacer l’occupation : l’équipe doit d’abord déplacer le patient.

La fin/transfert/annulation du passage est réconciliée en arrière-plan
(30 lits par lot, une minute entre les lots par défaut). Une indisponibilité du
service patient **ne libère pas le lit**. La libération manuelle reste immédiate.
Ce délai peut augmenter avec une grande file ; il n’existe pas de callback
synchrone bloquant la clôture du passage.

### Maintenance et comptabilité

Une maintenance suit Demande → En cours → Terminée. Le compte rendu indique
explicitement si le matériel est opérationnel ; sinon il reste en panne.
Coût et prochaine échéance sont conservés, sans générer une facture fictive.

La transmission comptable utilise une outbox PostgreSQL persistante :
10 biens par lot, 30 secondes entre lots, reprise après deux minutes en cas
d’indisponibilité. Le destinataire relit la source, compare sa version et importe
le même identifiant : les retries ne créent pas de doublons.

Dans **Comptabilité → Immobilisations**, le comptable qualifie chaque bien :
charge, immobilisation ou propriété d’un tiers. Il renseigne la valeur retenue,
la valeur résiduelle et les comptes appropriés. Aucun seuil ni durée fiscale
n’est imposé. Pour le plan linéaire mensuel : classe 2 hors 28/29 pour le bien,
28 pour l’amortissement, 68 pour la dotation ; comptes actifs du même hôpital.
Ces familles sont des contrôles techniques, pas une certification de conformité.

Le plan actuel est mensuel, **sans prorata journalier**, première échéance en
fin du mois de début. Les arrondis se compensent sur la durée. Une échéance
atteinte produit au plus un **brouillon équilibré**, à comptabiliser par le
validateur dans les journaux existants. Pas de comptabilisation automatique.
Après préparation d’une écriture, le plan est figé : une correction exige un
traitement comptable distinct, pas une réécriture des échéances passées.

Les corrections de l’intendance mettent à jour la source mais ne remplacent pas
la valeur déjà retenue par le comptable. Une nouvelle devise bloque la
préparation d’écritures jusqu’à examen. Acquisition, paiement fournisseur,
dépréciation exceptionnelle et sortie comptable restent dans le circuit
comptable existant : ils ne sont pas déduits arbitrairement de l’inventaire.

## API et listes

Préfixe organisation : `/api/v1/organizations/patrimony`.
Toutes les mutations requièrent un jeton et les droits du périmètre.

| Méthode | Route relative | Usage |
|---|---|---|
| GET | /categories | Catégories actives |
| GET | /{resource}/search | assets, locations, beds, operations, events, documents |
| POST | /locations | Bâtiment/étage/salle |
| PATCH | /locations/{id}/status | Activer/désactiver (protège les enfants/biens actifs) |
| POST / PUT | /assets / /assets/{id} | Enregistrer/modifier ; version requise en modification |
| GET | /assets/{id} | Dossier du bien |
| POST | /assets/{id}/assignment | Responsable actif ou null pour le retirer, justification |
| POST | /assets/{id}/movements | MOVE, LOAN, RETURN, FAULT, CLEANED, INVENTORY_FOUND, INVENTORY_MISSING |
| POST | /assets/{id}/operations | MAINTENANCE, RETIREMENT, LOSS |
| POST | /operations/{id}/decision | START, COMPLETE, REJECT |
| POST | /assets/{id}/documents | kind, fileName, contentType, base64 |
| GET | /documents/{id} | Contenu à télécharger |
| GET | /assets/{id}/label | QR SVG encodé, numéro d’inventaire |
| GET / POST | /beds/{id} / /beds/{id}/occupancy | Fiche minimale / réservation-occupation |
| POST | /beds/{id}/release | Libération justifiée |

Pagination : page indexée à zéro, size 1–100, query, hospitalId (admin),
status, category, locationId, assetId selon registre. Organisation renvoie
`items`, comptabilité `content` ; le BFF normalise les deux en `items`.
Le shell et les titres restent affichés ; seuls les contenus dynamiques
chargent. Les filtres conservent les résultats précédents pendant la recherche.

Préfixe comptable : `/api/v1/accounting/fixed-assets` :
`GET /search`, `GET /{id}`, `POST /{id}/classification`,
`GET /{id}/schedule?page=0&size=20`, `POST /{id}/installments`.
Le dernier reçoit `journalId` et `installment`, pas un numéro d’écriture.

Les routes `/internal/organizations/patrimony/assets/{id}/accounting-reference`
et `/internal/accounting/patrimony-assets/{id}` restent privées au réseau
des services, selon le modèle existant. Ne pas publier les ports des services
directement ni ajouter de route gateway `/internal`.

## Vérification

Tests unitaires usuels : `mvn -pl services/account,services/auth,services/organization,services/accounting test`.
Tests PostgreSQL, uniquement sur une **base jetable** :

```bash
PATRIMONY_POSTGRES_TEST=true \
SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:PORT/base_test \
SPRING_DATASOURCE_USERNAME=postgres SPRING_DATASOURCE_PASSWORD=mot_de_passe_test \
mvn -pl services/organization -Dtest=PatrimonyPersistenceTest test
```

Pour accounting, utiliser une autre base vide et
`-pl services/accounting -Dtest=FixedAssetPersistenceTest,FixedAssetAmountTest`.
Ces tests sont également exécutés par le job CI PostgreSQL.
Dans le front : `node --test tests/patrimony-actions.test.cjs`, `npm run build`.
