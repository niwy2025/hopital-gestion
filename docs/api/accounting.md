# Comptabilité hospitalière

Le service `accounting-service` porte la comptabilité de chaque hôpital. Il
utilise un socle de plan comptable inspiré du SYSCOHADA révisé, mais les
paramètres d'imputation restent configurables par l'équipe financière de
l'établissement. Cette précaution est importante : les comptes réellement
retenus, les taxes et les règles de clôture doivent être validés par le
comptable de l'hôpital.

## Périmètre et contrôle d'accès

Les comptes non administrateurs accèdent exclusivement aux données de leur
hôpital, déterminé par leur affectation principale. Un administrateur central
peut sélectionner un établissement lorsque cela est nécessaire. Le serveur ne
fait jamais confiance à un identifiant d'hôpital fourni seul par le navigateur.

| Rôle | Responsabilité principale |
| --- | --- |
| `BILLING_OFFICER` | Préparer les factures à soumettre au responsable financier. |
| `CASHIER` | Ouvrir/fermer la caisse et enregistrer les encaissements. |
| `HOSPITAL_ACCOUNTANT` | Préparer les écritures, consulter les journaux et préparer les annexes. |
| `FINANCE_MANAGER` | Valider les factures et écritures, clôturer les périodes et valider les annexes. |
| `FINANCE_AUDITOR` | Lire les journaux, pièces, rapports et traces d'audit. |

Les droits fins sont retournés par `account-service` (`ACCOUNTING_*`) et sont
contrôlés à la fois par le BFF et par le service Comptabilité.

## Principes métier

- Chaque écriture possède au moins deux lignes équilibrées : débit = crédit.
- Une écriture validée est immuable. Une correction est faite par une
  contrepassation, jamais par modification ou suppression de l'original.
- Les périodes clôturées ne reçoivent plus de nouvelles écritures.
- Une facture, un encaissement et une pièce justificative gardent leur code de
  référence et leur auteur pour l'audit.
- Les annexes des états financiers sont distinctes des pièces justificatives
  d'une facture ou d'un encaissement.
- Au premier usage d'un hôpital, le socle initialise son plan, ses journaux et
  l'exercice civil courant ; le responsable financier peut ensuite compléter
  les périodes et le paramétrage adaptés à l'établissement.

## Intégration Pharmacie

Une délivrance validée dans le parcours pharmacie est reprise de manière
idempotente à partir de son code `DSP-…`. Le service Comptabilité relit les
références internes du patient et de la pharmacie ; le navigateur ne transmet
ni prix, ni montant, ni coût de stock à cette intégration.

Le traitement crée ou retrouve :

1. la facture liée au patient/passage/ordonnance ;
2. l'encaissement correspondant au montant effectivement reçu ;
3. l'écriture de vente ;
4. l'écriture de coût de sortie, à partir du coût réellement sorti du stock.

Le flux est placé dans une file interne persistante lorsque le service
Comptabilité est indisponible. Les reprises sont sûres grâce à l'unicité de la
source `PHARMACY_DISPENSE + DSP-…`.

Une réception de stock pharmacie est traitée de la même manière à partir de
son code `ENT-…`, sans laisser le navigateur choisir le montant. Elle génère
une écriture d'achat `débit 310000 – Stock de médicaments` et `crédit 401100 –
Fournisseurs` lorsque le fournisseur est renseigné. Sans fournisseur, le
crédit va temporairement au compte de rapprochement `408100` afin de ne pas
inventer de tiers. La file durable est idempotente par
`PHARMACY_STOCK_RECEIPT + ENT-…` et la réception reste valide si la
comptabilité est indisponible.

Les sorties de stock hors délivrance patient sont elles aussi reprises depuis
leur code `MVT-…`, toujours par une file persistante et idempotente. Elles ne
créent jamais artificiellement une vente ou un encaissement : une perte est
portée au compte `658100`, une péremption au compte `658200`, un transfert
sortant au compte transitoire `382000` et une délivrance manuelle sans
contexte patient/prix au compte transitoire `471100`, contre le stock
`310000`. Les délivrances de prescription (`DSP-…`) sont délibérément exclues
de ce flux car elles sont déjà traitées par la facture, l'encaissement et le
coût de sortie du parcours patient.

## API publique

Toutes les routes publiques passent par l'API Gateway et demandent un jeton
d'accès valide. Les listes utilisent `page`, `size` et `query`; elles sont
paginées côté serveur.

| Route | Usage |
| --- | --- |
| `GET /api/v1/accounting/dashboard` | Indicateurs de l'hôpital courant. |
| `GET /api/v1/accounting/invoices/search` | Factures et état de paiement. |
| `GET /api/v1/accounting/payments/search` | Encaissements et autres paiements. |
| `GET /api/v1/accounting/entries/search` | Écritures comptables, avec statut et période. |
| `GET /api/v1/accounting/journals/search` | Référentiel des journaux. |
| `GET /api/v1/accounting/ledger/search` | Grand livre, filtrable par compte/période. |
| `GET /api/v1/accounting/trial-balance` | Balance générale. |
| `GET /api/v1/accounting/financial-statements` | Bilan, résultat et flux de trésorerie calculés depuis les écritures validées. |
| `GET /api/v1/accounting/notes/search` | Annexes/notes aux états financiers. |
| `GET /api/v1/accounting/periods/search` | Exercices et périodes de clôture. |
| `GET /api/v1/accounting/cash-sessions/search` | Ouvertures, fermetures et écarts de caisse. |

Les opérations de saisie suivent la séparation des tâches :

- `POST /invoices`, puis `POST /invoices/{id}/issue` ; un caissier enregistre
  ensuite `POST /invoices/{id}/payments`.
- `POST /entries` prépare une écriture ; seul le responsable financier la
  poste ou la contrepasse.
- `POST /notes`, `PUT /notes/{id}` et `POST /notes/{id}/validate` gèrent les
  annexes.
- `POST /cash-sessions/open` et `POST /cash-sessions/{id}/close` contrôlent
  la caisse par devise.

Les justificatifs de facture, règlement, écriture ou annexe sont envoyés sous
forme base64 (maximum 3 Mo) sur leur ressource correspondante ; leur contenu
reste privé et n'est jamais servi par le gateway sans contrôle de rôle.

Un paramètre `hospitalId` n'est interprété que pour un administrateur central.
Les utilisateurs d'hôpital ne peuvent pas s'en servir pour sortir de leur
périmètre.

## Annexes SYSCOHADA

Les annexes sont des notes structurées rattachées à une période : méthode
d'évaluation, immobilisations, stocks, créances et dettes, trésorerie,
engagements, personnel et événements postérieurs. Elles possèdent un statut de
brouillon puis de validation. Les montants de référence doivent être rapprochés
des états calculés à partir des écritures validées ; l'annexe conserve le texte
explicatif et ses justificatifs, pas un total financier isolé non traçable.

Le socle couvre l'exploitation hospitalière et la traçabilité requise pour les
états du système normal. Il ne remplace pas la revue d'un professionnel pour
la conformité fiscale, sociale et sectorielle propre à chaque établissement.
