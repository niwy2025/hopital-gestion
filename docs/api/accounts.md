# API Account Service

Documentation initiale du service de comptes, rôles et permissions.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/accounts` | Liste les comptes utilisateurs. |
| `GET` | `/api/v1/accounts/search` | Liste paginée, filtrable par recherche et hôpital. |
| `POST` | `/api/v1/accounts` | Crée un compte avec un ou plusieurs rôles. |
| `GET` | `/api/v1/accounts/{accountId}` | Retourne la fiche complète d'un compte. |
| `PUT` | `/api/v1/accounts/{accountId}` | Modifie les informations, les rôles, l'hôpital et éventuellement le mot de passe. |
| `GET` | `/api/v1/accounts/identifier/{identifier}` | Recherche un compte par email ou username. |
| `GET` | `/api/v1/accounts/roles` | Liste les rôles et permissions disponibles. |
| `POST` | `/internal/accounts/validate-credentials` | Valide un couple identifiant/mot de passe pour `auth-service`. |

Un identifiant peut être soit un email, soit un username.

## Rattachement à l'hôpital

`hospitalId` est un UUID facultatif qui désigne un hôpital géré par
`organization-service`. Il n'existe pas de clé étrangère SQL entre les deux
bases : chaque service reste propriétaire de son schéma.

Un compte sans `hospitalId` peut être créé afin de préparer une affectation,
mais il ne peut pas se connecter tant qu'il n'est pas administrateur. Un compte
portant le rôle `ADMIN` est l'unique exception, notamment pour l'administration
provinciale avant l'affectation d'un établissement.

Exemple de création :

```json
{
  "username": "docteur.kanku",
  "email": "kanku@hopital.cd",
  "displayName": "Dr Kanku",
  "password": "mot-de-passe-temporaire",
  "hospitalId": "c6c98b9b-2c28-44be-a81d-7c125c74483c",
  "roles": ["DOCTOR"]
}
```

Pour une mise à jour, le champ `password` est facultatif. Lorsqu'il est absent,
le mot de passe actuel est conservé.

## Comptabilité hospitalière

Les rôles comptables sont prévus pour une affectation à un hôpital. Comme tous
les comptes opérationnels, ils doivent donc disposer d'un `hospitalId` avant de
pouvoir se connecter. L'administration provinciale (`ADMIN`) reste la seule
exception transversale.

| Rôle | Responsabilité | Restrictions de contrôle |
| --- | --- | --- |
| `BILLING_OFFICER` | Préparer les factures | N'encaisse pas et ne valide pas. |
| `CASHIER` | Ouvrir/fermer sa caisse et enregistrer les encaissements | Ne modifie ni ne valide les factures. |
| `HOSPITAL_ACCOUNTANT` | Tenir les journaux, produire les états et préparer les annexes | Ne valide pas ses propres écritures. |
| `FINANCE_MANAGER` | Contrôler les écritures, valider, clôturer les périodes et annexes | N'assure pas les opérations de caisse. |
| `FINANCE_AUDITOR` | Consulter les pièces, journaux, rapports et traces | Lecture seule. |

Les permissions commencent toutes par `ACCOUNTING_`. Elles couvrent le plan
comptable, les journaux, factures, paiements, caisses, rapports, journal d'audit,
clôture de période et annexes des états financiers. Cette granularité permet au
service Comptabilité de respecter la séparation entre saisie, encaissement
et validation requise par une gestion SYSCOHADA traçable.

| Domaine | Permissions |
| --- | --- |
| Référentiel | `ACCOUNTING_CHART_READ`, `ACCOUNTING_CONFIGURATION_WRITE` |
| Journaux | `ACCOUNTING_JOURNAL_READ`, `ACCOUNTING_JOURNAL_WRITE`, `ACCOUNTING_JOURNAL_VALIDATE` |
| Facturation | `ACCOUNTING_INVOICE_READ`, `ACCOUNTING_INVOICE_WRITE`, `ACCOUNTING_INVOICE_VALIDATE` |
| Encaissements | `ACCOUNTING_PAYMENT_READ`, `ACCOUNTING_PAYMENT_WRITE` |
| Caisse | `ACCOUNTING_CASH_READ`, `ACCOUNTING_CASH_OPEN`, `ACCOUNTING_CASH_CLOSE` |
| États et contrôle | `ACCOUNTING_REPORT_READ`, `ACCOUNTING_AUDIT_READ`, `ACCOUNTING_PERIOD_CLOSE` |
| Annexes | `ACCOUNTING_ANNEX_READ`, `ACCOUNTING_ANNEX_WRITE`, `ACCOUNTING_ANNEX_VALIDATE` |

## Rôles de prescription et de pharmacie

- **Médecin** : crée les ordonnances médicales de ses patients.
- **Pharmacien·ne** : consulte les ordonnances et enregistre une ordonnance
  externe présentée sur papier ; la délivrance des médicaments sera rattachée à
  cette ordonnance dans le module Pharmacie.

L’ordonnance externe reste donc identifiable dans l’audit et ne se fait jamais
passer pour une prescription rédigée par un médecin de la plateforme.

## Photo de profil

La fiche `GET /api/v1/accounts/{accountId}` retourne la photo uniquement pour
un compte précis, sous la forme de `profilePhotoBase64` et
`profilePhotoContentType`. Les listes ne renvoient pas ces champs afin de ne
pas transporter les images de tous les utilisateurs.

À la création ou à la modification, une photo peut être envoyée avec les deux
champs suivants :

```json
{
  "profilePhotoBase64": "<base64 sans préfixe data:>",
  "profilePhotoContentType": "image/jpeg"
}
```

Les formats JPEG, PNG et WebP sont acceptés, dans une limite de **512 Ko une
fois décodée**. Pour supprimer une photo existante à la modification, envoyez
`"removeProfilePhoto": true` sans les champs de photo.
