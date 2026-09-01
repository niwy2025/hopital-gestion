# Laboratory Service

Service métier du laboratoire. Il enregistre et trace le cycle complet d'une
analyse : demande, réception de l'échantillon, saisie du résultat et validation
par le biologiste.

Les identifiants de patient et de laboratoire sont conservés comme références
externes. Une demande indique si l’analyse est exécutée dans un laboratoire
interne d’hôpital ou dans un laboratoire provincial de référence. Après la
réception de l’échantillon, son statut signale que le dossier est au laboratoire
en attente de résultat. Une demande créée depuis un passage patient porte aussi
la référence UUID de ce passage ; le service Patients reste la source de vérité
du patient et de l’épisode, tandis qu’Organisation valide le laboratoire interne
actif de l’hôpital.

## Endpoints

- `GET` / `POST /api/v1/laboratory/analysis-requests`
- `GET /api/v1/laboratory/analysis-requests/search?page=0&size=20&query=...`
- `GET` / `POST /api/v1/laboratory/specimens`
- `GET /api/v1/laboratory/specimens/search?page=0&size=20&query=...`
- `GET` / `POST /api/v1/laboratory/analysis-results`
- `GET /api/v1/laboratory/analysis-results/search?page=0&size=20&query=...`
- `PATCH /api/v1/laboratory/analysis-results/{code}/validation`
