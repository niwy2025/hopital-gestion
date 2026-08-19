# API Account Service

Documentation initiale du service de comptes, rôles et permissions.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/accounts` | Liste les comptes utilisateurs. |
| `POST` | `/api/v1/accounts` | Crée un compte avec un ou plusieurs rôles. |
| `GET` | `/api/v1/accounts/identifier/{identifier}` | Recherche un compte par email ou username. |
| `GET` | `/api/v1/accounts/roles` | Liste les rôles et permissions disponibles. |
| `POST` | `/internal/accounts/validate-credentials` | Valide un couple identifiant/mot de passe pour `auth-service`. |

Un identifiant peut être soit un email, soit un username.
