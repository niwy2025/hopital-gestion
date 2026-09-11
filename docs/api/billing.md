# Prestations, tarifs et frais de passage — premier lot

Le service Comptabilité (`accounting-service`) gère le catalogue **par hôpital**,
les tarifs datés et les frais réalisés à préparer pour facturation. Le service
Patient fournit uniquement la référence administrative du passage à cet usage.

Ce lot ne transforme pas les lignes en factures, n'encaisse pas, ne génère pas
d'écriture comptable et ne calcule pas automatiquement les journées/nuitées.
Le total « à facturer » n'est donc **pas un solde dû**. Le circuit de facturation
et de règlement des délivrances de pharmacie existant reste indépendant : ne
pas recopier ses ventes dans ces frais manuels.

## Parcours front

1. Comptabilité → **Prestations et tarifs** : créer une prestation, puis ajouter
   son tarif dans une devise et une unité. Les codes sont générés côté serveur.
2. Comptabilité → **Frais des passages** : rechercher par code du passage ou
   identité administrative du patient. L'hôpital vient de l'affectation, ou de
   la sélection de l'administrateur central.
3. **Voir les frais** → **Ajouter une prestation réalisée** : sélectionner une
   prestation active via recherche serveur, préciser date, quantité et devise.
   Le serveur calcule l'aperçu. Aucune saisie libre du prix n'est acceptée.
4. Confirmer l'ajout. Les frais, leurs auteurs et leurs annulations sont visibles
   directement dans la fiche. Les totaux non annulés sont séparés par devise et
   couvrent tout le passage, indépendamment des filtres du tableau.

Routes front : `/comptabilite/prestations`, `/comptabilite/frais-passages` et
`/facturation-passages/[id]`. Le lien depuis le dossier du passage est visible
seulement si l'utilisateur possède le droit de consulter la facturation.

## Rôles et périmètre

| Opération | Rôles existants |
| --- | --- |
| Lire catalogue, tarifs et frais | ADMIN, HOSPITAL_ADMIN, HOSPITAL_ACCOUNTANT, FINANCE_MANAGER, FINANCE_AUDITOR, BILLING_OFFICER, CASHIER |
| Créer/modifier une prestation, gérer les tarifs | ADMIN, FINANCE_MANAGER |
| Ajouter/annuler des frais non facturés | ADMIN, FINANCE_MANAGER, BILLING_OFFICER |

Le BFF vérifie les permissions `ACCOUNTING_INVOICE_READ`,
`ACCOUNTING_CONFIGURATION_WRITE`, `ACCOUNTING_INVOICE_WRITE`. Le backend
contrôle à nouveau le rôle et l'affectation. `provinceWide` seul ne donne pas
le droit de facturer un autre hôpital : l'exception est réservée à ADMIN.
Le rôle de médecin seul ne permet pas de saisir les frais ni les tarifs.
Aucun nouveau rôle ou droit de lecture clinique n'est attribué par ce lot.

## Règles et traçabilité

- UUID pour toutes les clés ; codes `PRS-…` et `FRS-…` générés par le serveur.
- Nom de prestation unique dans un hôpital, sans distinction de casse.
- Catégories : consultation, laboratoire, soins, hospitalisation, autre.
- Tarifs CDF ou USD, sans conversion ; unités : acte, jour, nuit, heure, forfait.
- Le dernier tarif non annulé avec `effectiveFrom <= performedOn` s'applique,
  dans la devise demandée. Le prix peut être nul ; deux décimales au maximum.
- Ajout à partir d'aujourd'hui uniquement. Un tarif futur peut être annulé avec
  motif ; un tarif déjà entré en vigueur n'est ni modifié ni supprimé. Une
  nouvelle date d'effet permet le changement suivant. Les références datées
  du passé nécessitent un tarif déjà applicable à cette date.
- Le jour métier est celui d'`Africa/Kinshasa` (propriété backend
  `hospital.billing.zone`). Le front utilise le même calendrier.
- La date de réalisation doit être comprise entre l'arrivée et la fin du
  passage, et ne pas être future. Après une sortie clinique ou un transfert,
  une prestation antérieure peut encore être enregistrée. Passage annulé :
  aucun nouveau frais. Ce lot n'ajoute pas de blocage financier de sortie.
- Quantité positive, trois décimales au maximum. Calcul `BigDecimal`, arrondi
  HALF_UP à deux décimales sur le total de la ligne, limite NUMERIC(16,2).
- Le prix, l'unité, la désignation et les références patient/passage sont figés
  dans chaque ligne. Les prix et les identifiants opérateur ne viennent jamais
  du navigateur. Une annulation conserve auteur, date et motif ; pas de DELETE.
- `requestId` est une clé technique UUID de reprise, pas un numéro métier.
  Même clé + même contenu = même ligne, même en concurrence. Clé réutilisée
  avec un contenu différent = HTTP 409. Une nouvelle prestation utilise une
  nouvelle clé. Le front conserve cette clé lors d'une reprise sans modification.
- La saisie transmet le `tariffId` de l'aperçu : un tarif devenu inapplicable
  provoque un conflit à résoudre avant enregistrement.

## API publique

Toutes les routes commencent par `/api/v1/accounting/billing` et passent par le
gateway. `page` commence à zéro, `size` est compris entre 1 et 100 (20 par défaut).
Les listes comptables renvoient `content`, `page`, `size`, `totalElements`,
`totalPages` ; le BFF les normalise en `items`. La recherche de passages reprend
le format `items` du service Patient.

| Méthode / chemin | Usage |
| --- | --- |
| GET `/services/search` | hospitalId, page, size, query, category, active |
| POST `/services` | hospitalId, name, category, description |
| GET `/services/{id}` | Fiche catalogue |
| PUT `/services/{id}` | name, category, description, active, version, reason |
| GET `/services/{id}/tariffs/search` | Historique paginé des tarifs |
| POST `/services/{id}/tariffs` | effectiveFrom, currency, unit, unitPrice, reason |
| POST `/services/{id}/tariffs/{tariffId}/cancel` | reason, tarif futur seulement |
| GET `/services/{id}/events/search` | Historique paginé du catalogue |
| GET `/passages/search` | hospitalId, page, size, query ; référence administrative |
| GET `/passages/{id}` | page, size, query, status ; passage, charges et totals |
| GET `/passages/{id}/quote` | serviceId, performedOn, quantity, currency |
| POST `/passages/{id}/charges` | serviceId, tariffId, performedOn, quantity, currency, requestId, note |
| POST `/passages/{id}/charges/{chargeId}/cancel` | reason |

Les routes privées Patient `/internal/patients/billing/passages/{id}` et
`/internal/patients/billing/passages/search?hospitalId=…` n'exposent ni motif de
consultation, ni diagnostic, ni journal clinique. Elles ne doivent pas être
publiées par le proxy Internet.

## Déploiement et vérifications

Rebuilder **patient-service et accounting-service**, puis le front. Aucun reset
de base ni changement `.env` n'est nécessaire. La migration additive
`V4_add_services_tariffs_and_passage_charges.sql` crée les tables comptables.
La migration Patient `V22_index_billing_passage_search.sql` indexe le registre
par hôpital et date d'arrivée sans modifier les données existantes.
Les prix sont propres à l'établissement : aucune valeur financière n'est semée.

```bash
docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml up -d --build patient-service accounting-service
```

Le catalogue est vide au premier démarrage. Un responsable financier doit créer
les prestations et leurs tarifs avant que l'agent de facturation puisse les saisir.
Dans Postman, les collections par service et globale contiennent un dossier
« Prestations, tarifs et frais à facturer ». Les prix d'exemple sont fictifs.

Tests : `BillingPersistenceTest` sur PostgreSQL avec
`PATRIMONY_POSTGRES_TEST=true` et `SPRING_DATASOURCE_*` pointant exclusivement
vers une base de test ; tests unitaires `BillingAmountTest` et
`BillingAuthorizationTest`. Les tests Patient vérifient la recherche minimale
par hôpital et l'absence de recherche sur les données cliniques.

Suite à développer : sélection des frais en facture avec verrouillage/idempotence,
annulations/avoirs, puis règlement, écritures comptables et rapprochement des
prestations produites automatiquement par les autres modules.
