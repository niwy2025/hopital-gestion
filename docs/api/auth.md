# API Auth Service

Préversion de la documentation des endpoints d'authentification.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Authentifie un utilisateur via Keycloak et retourne un token JWT. |
| `POST` | `/api/v1/auth/refresh` | Renouvelle un token avec un refresh token. |
| `GET` | `/api/v1/auth/me` | Retourne le profil de l'utilisateur courant. |

Les contrats détaillés seront complétés au moment de l'implémentation métier.
