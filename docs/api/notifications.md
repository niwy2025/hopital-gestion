# Notifications API

Les diffusions passent par l'API publique et sont traitées de manière
asynchrone par Kafka.

| Méthode | Chemin | Description |
| --- | --- | --- |
| `POST` | `/api/v1/notifications/broadcasts` | Ajoute un broadcast e-mail/SMS à la file. |

Une demande valide reçoit `202 Accepted` avec l'identifiant de notification et
le statut `QUEUED`. Le corps contient les champs `type`, `channels`,
`recipients`, `subject`, `body` et, facultativement, `metadata`.

Les canaux actuellement disponibles sont `EMAIL` et `SMS`. Les connecteurs sont
pour l'instant des adaptateurs de démonstration : les envois sont tracés dans
les logs de `notification-service` et pourront être remplacés par un relais SMTP
et un fournisseur SMS sans modifier les producteurs Kafka.
