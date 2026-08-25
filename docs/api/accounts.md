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
