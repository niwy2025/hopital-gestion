# API Pharmacie — catalogue, stock et mouvements

Le service est exposé uniquement à travers l’API Gateway sous
`/api/v1/pharmacy`. Les codes de médicament (`MED-…`) et d’entrée de stock
(`ENT-…`) et de mouvement (`MVT-…`) sont générés par le backend.

| Méthode | Route | Usage |
| --- | --- | --- |
| `GET` | `/api/v1/pharmacy/medicines/search?page=0&size=20&query=amoxicilline&active=true` | Catalogue paginé des médicaments. |
| `POST` | `/api/v1/pharmacy/medicines` | Crée un médicament du catalogue. |
| `GET` | `/api/v1/pharmacy/stocks/search?page=0&size=20&query=amoxicilline&lowStock=true` | Stock courant limité au périmètre de l’utilisateur. |
| `GET` | `/api/v1/pharmacy/stocks/medicines/{medicineId}` | Stock actuel du médicament dans l’hôpital de l’utilisateur, pour préparer une entrée ou une sortie. |
| `GET` | `/api/v1/pharmacy/stock-entries/search?page=0&size=20&accountingStatus=PENDING_ACCOUNTING` | Historique valorisé des entrées à comptabiliser. |
| `POST` | `/api/v1/pharmacy/stock-entries` | Enregistre une réception et met à jour le stock de l’hôpital. |
| `GET` | `/api/v1/pharmacy/stock-movements/search?page=0&size=20&type=LOSS` | Journal paginé des entrées, sorties et péremptions. |
| `POST` | `/api/v1/pharmacy/stock-exits` | Enregistre une délivrance manuelle, un transfert ou une perte en consommant les lots valides les plus proches de leur péremption. |
| `POST` | `/api/v1/pharmacy/stocks/expire` | Sort les lots dont la date de péremption est dépassée et conserve la trace de destruction. |

Chaque entrée conserve la quantité, le coût unitaire, le total, la monnaie,
l’auteur et le statut `PENDING_ACCOUNTING`. Le futur service Comptabilité
pourra reprendre ces lignes immuables sans recalculer le coût d’origine.

Une ligne de stock est unique pour un couple hôpital–médicament. Les lots issus
des entrées conservent leurs quantités restantes et dates de péremption. La
quantité « disponible » exclut les lots périmés, même avant leur sortie
définitive dans le journal.
