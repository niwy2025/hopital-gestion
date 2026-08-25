# Laboratory Service

Service métier du laboratoire. Il enregistre et trace le cycle complet d'une
analyse : demande, réception de l'échantillon, saisie du résultat et validation
par le biologiste.

Les identifiants de patient, de laboratoire de référence et de structure sont
conservés comme références externes. Le futur service Patients deviendra la
source de vérité des patients ; le référentiel Organisation reste la source de
vérité des laboratoires.

## Endpoints

- `GET` / `POST /api/v1/laboratory/analysis-requests`
- `GET` / `POST /api/v1/laboratory/specimens`
- `GET` / `POST /api/v1/laboratory/analysis-results`
- `PATCH /api/v1/laboratory/analysis-results/{code}/validation`
