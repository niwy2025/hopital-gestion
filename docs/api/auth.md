# API Auth Service

Le navigateur communique uniquement avec l'application. `auth-service` valide
d'abord les identifiants auprès de `account-service`, synchronise le compte et
ses rôles avec Keycloak côté serveur, puis retourne les jetons émis. Keycloak
ne constitue donc pas une interface de connexion exposée aux utilisateurs.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Valide les identifiants et ouvre une session. |
| `POST` | `/api/v1/auth/refresh` | Renouvelle une session avec son refresh token. |
| `GET` | `/api/v1/auth/me` | Retourne le compte courant, ses droits et ses appareils reconnus. |
| `GET` | `/api/v1/auth/health` | Vérifie la disponibilité du service. |

## Connexion

```json
POST /api/v1/auth/login
{
  "username": "<HOSPITAL_ADMIN_USERNAME>",
  "password": "<HOSPITAL_ADMIN_PASSWORD>"
}
```

Réponse `200 OK` :

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<refresh-token>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "expiresAt": "2026-08-24T14:15:13Z",
  "refreshExpiresIn": 28800,
  "refreshExpiresAt": "2026-08-24T22:00:13Z",
  "userAgent": "PostmanRuntime/…"
}
```

La durée de vie de l'access token est de **15 minutes**. Le refresh token est
valide **8 heures** et il est renouvelé à chaque appel de rafraîchissement.

## Premier administrateur

Au premier démarrage, `account-service` crée le compte défini par
`HOSPITAL_ADMIN_USERNAME`, `HOSPITAL_ADMIN_EMAIL` et
`HOSPITAL_ADMIN_PASSWORD` dans le fichier `.env`. En production, ces trois
variables sont obligatoires. Leur modification ne change jamais un compte déjà
créé : le mot de passe doit ensuite être modifié depuis l'administration des
utilisateurs.

Après connexion, la page `/utilisateurs` permet à un administrateur de créer
d'autres comptes et de leur attribuer le rôle `ADMIN`.

Le header HTTP `User-Agent` est automatiquement conservé dans le journal
`hospital_auth.dbo.auth_login_audits`, avec le statut de la connexion et la
date d'expiration de l'access token. Il s'agit d'une donnée de traçabilité :
elle peut être falsifiée par un client et ne constitue pas une preuve
d'identité.

## Renouvellement

```json
POST /api/v1/auth/refresh
{
  "refreshToken": "<refresh-token>"
}
```

La réponse a exactement le même format que celle de connexion. Le client doit
remplacer les deux jetons et leurs dates d'expiration par les nouvelles valeurs.

## Espace compte

`GET /api/v1/auth/me` requiert `Authorization: Bearer <access-token>`. La
réponse contient l'identifiant, le nom, l'e-mail, les rôles et permissions du
compte ainsi que les appareils reconnus. Un appareil est regroupé par
`User-Agent` et expose sa dernière connexion réussie et son nombre de
connexions. Le User-Agent est une information déclarative et peut être imité ;
il ne remplace pas une solution de gestion de sessions ou de révocation.
