# API Auth Service

Préversion de la documentation des endpoints d'authentification.

| Méthode | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Authentifie un utilisateur par email ou username et retourne token, rôles et permissions. |
| `POST` | `/api/v1/auth/refresh` | Renouvelle un token avec un refresh token. |
| `GET` | `/api/v1/auth/me` | Retourne le profil de l'utilisateur courant. |

Exemple de login :

```json
{
  "identifier": "admin@hopital.local",
  "password": "admin123"
}
```

Le champ `identifier` peut être remplacé par `email` ou `username` pour les clients qui préfèrent séparer les modes de connexion.

Les contrats détaillés seront complétés au moment de l'implémentation métier.
