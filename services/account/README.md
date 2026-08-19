# Account Service

Microservice de gestion des comptes, rôles et permissions. Il accepte la
recherche d'un utilisateur par email ou par username et expose un endpoint
interne utilisé par `auth-service` pour valider les identifiants.

## Endpoints initiaux

- `GET /api/v1/accounts` : liste les comptes.
- `POST /api/v1/accounts` : crée un compte avec ses rôles.
- `GET /api/v1/accounts/identifier/{identifier}` : retrouve un compte par email ou username.
- `GET /api/v1/accounts/roles` : liste les rôles et permissions disponibles.
- `POST /internal/accounts/validate-credentials` : endpoint interne pour l'authentification.
