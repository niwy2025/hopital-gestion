# API Pharmacie — catalogue et stock

Le service est exposé uniquement à travers l’API Gateway sous
`/api/v1/pharmacy`. Les codes de médicament (`MED-…`) et d’entrée de stock
(`ENT-…`) sont générés par le backend.

| Méthode | Route | Usage |
| --- | --- | --- |
| `GET` | `/api/v1/pharmacy/medicines/search?page=0&size=20&query=amoxicilline&active=true` | Catalogue paginé des médicaments. |
| `POST` | `/api/v1/pharmacy/medicines` | Crée un médicament du catalogue. |
| `GET` | `/api/v1/pharmacy/stocks/search?page=0&size=20&query=amoxicilline&lowStock=true` | Stock courant limité au périmètre de l’utilisateur. |
| `GET` | `/api/v1/pharmacy/stock-entries/search?page=0&size=20&accountingStatus=PENDING_ACCOUNTING` | Historique valorisé des entrées à comptabiliser. |
| `POST` | `/api/v1/pharmacy/stock-entries` | Enregistre une réception et met à jour le stock de l’hôpital. |

Chaque entrée conserve la quantité, le coût unitaire, le total, la monnaie,
l’auteur et le statut `PENDING_ACCOUNTING`. Le futur service Comptabilité
pourra reprendre ces lignes immuables sans recalculer le coût d’origine.
