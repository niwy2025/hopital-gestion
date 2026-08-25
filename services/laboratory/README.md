# Laboratory Service

Service métier du laboratoire. Il enregistre et trace le cycle complet d'une
analyse : demande, réception de l'échantillon, saisie du résultat et validation
par le biologiste.

Les identifiants de patient et de laboratoire sont conservés comme références
externes. Une demande indique si l’analyse est exécutée dans un laboratoire
interne d’hôpital ou dans un laboratoire provincial de référence. Après la
réception de l’échantillon, son statut signale que le dossier est au laboratoire
en attente de résultat. Le futur service Patients deviendra la source de vérité
des patients ; le référentiel Organisation reste la source de vérité des
laboratoires.

## Endpoints

- `GET` / `POST /api/v1/laboratory/analysis-requests`
- `GET` / `POST /api/v1/laboratory/specimens`
- `GET` / `POST /api/v1/laboratory/analysis-results`
- `PATCH /api/v1/laboratory/analysis-results/{code}/validation`
