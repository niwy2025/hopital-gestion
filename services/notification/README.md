# Notification Service

Service asynchrone chargé d'envoyer les notifications e-mail et SMS. Les autres
services publient un message JSON sur le topic Kafka
`hospital.notification.request.v1`; ce service le consomme et sélectionne le
canal demandé.

## Endpoint de diffusion

- `POST /api/v1/notifications/broadcasts` : ajoute un broadcast à la file Kafka.

Exemple de corps :

```json
{
  "type": "APPOINTMENT_REMINDER",
  "channels": ["EMAIL", "SMS"],
  "recipients": [
    {
      "email": "patient@hopital.local",
      "phoneNumber": "+243810000000",
      "displayName": "Patient"
    }
  ],
  "subject": "Rappel de rendez-vous",
  "body": "Votre rendez-vous est prévu demain."
}
```

L'API répond immédiatement `202 Accepted`. Kafka porte la file d'attente et le
consommateur traite les messages séparément. Les connecteurs e-mail et SMS sont
pour le moment des adaptateurs de démonstration qui consignent l'envoi dans les
logs ; ils sont prêts à être remplacés par SMTP et un fournisseur SMS.
