# Hopital Gestion

Monorepo de départ pour une architecture microservices de gestion d'hôpitaux.
Le dépôt fournit le socle d'infrastructure, un API Gateway Spring Boot, un
service `auth`, la documentation API et une collection Postman.

## Arborescence principale

```text
docs/api/                         Documentation API par domaine
infrastructure/docker/            Scripts et fichiers Docker transverses
infrastructure/gateway/api-gateway/ Gateway Spring Boot / Spring Cloud Gateway
infrastructure/keycloak/          Realm Keycloak hospital
infrastructure/kong/              Notes et commandes Kong Admin API
infrastructure/monitoring/        Prometheus, Grafana, dashboards et provisioning
postman/                          Collections Postman
services/account/                 Microservice de gestion des comptes
services/auth/                    Microservice d'authentification
services/notification/            Microservice asynchrone e-mail et SMS
services/organization/            Référentiel territorial, hôpitaux et laboratoires
services/laboratory/              Cycle des analyses, échantillons, résultats et validations
services/pharmacy/                Catalogue, stock et délivrances de médicaments
services/patient/                 Registre provincial des dossiers patients
services/personnel/               Dossiers, documents et affectations du personnel
services/accounting/              Facturation, caisse, journaux et états comptables par hôpital
```

## Briques incluses

- **Spring Boot** pour les services Java (`api-gateway`, `auth-service`, `account-service`, `notification-service`, `organization-service`, `laboratory-service`, `patient-service`, `pharmacy-service`, `personnel-service`, `accounting-service`).
- **Docker Compose** pour orchestrer les dépendances et services locaux.
- **Kong** comme gateway interne, conservée pour les intégrations qui l'utilisent.
- **Keycloak** pour OAuth2/OpenID Connect et l'émission des JWT.
- **PostgreSQL** comme persistance principale des services, avec une base logique par microservice.
- **Kafka** pour les échanges asynchrones.
- **Redis** pour le cache et le rate limiting.
- **Prometheus + Grafana** pour les métriques et dashboards.
- **Postman** pour tester les endpoints exposés.

## Démarrage rapide

```bash
cp .env.example .env
docker compose up -d
```

## Builds Docker incrémentaux

Les Dockerfiles Java séparent les `pom.xml` du code source et utilisent un
cache Maven BuildKit partagé. Une modification dans `src/` recompile seulement
le service concerné ; les dépendances ne sont résolues à nouveau que lorsqu'un
`pom.xml` change (ou lors du premier build sur une machine).

Pour reconstruire et redémarrer un seul service après une modification :

```bash
docker compose build auth-service
docker compose up -d --no-deps --force-recreate auth-service
```

Remplacez `auth-service` par `account-service`, `notification-service`,
`organization-service`, `laboratory-service`, `patient-service`,
`pharmacy-service`, `personnel-service`, `accounting-service` ou
`api-gateway` selon le service modifié. Évitez
`--no-cache` pour le développement courant : cette option force volontairement
le téléchargement et la reconstruction de toutes les couches.

### Ressources et stabilité Docker

Les conteneurs ont des plafonds mémoire, un heap JVM borné et des logs rotatifs
afin de préserver la mémoire et le disque de l'hôte. PostgreSQL remplace SQL
Server : une seule instance héberge une base logique par service, ce qui réduit
fortement la mémoire minimale. Les valeurs par défaut figurent dans
`.env.example` et `.env.production.example`. Un VPS de 4 Go convient aux tests
et à une faible charge ; 8 Go restent recommandés pour une production avec tous
les services, Kafka, Keycloak et l'observabilité actifs.

Pour éviter de bloquer le VPS pendant une reconstruction, limitez le nombre de
builds parallèles et ne construisez que les services modifiés :

```bash
# Un seul build à la fois : plus lent, mais le VPS reste réactif.
docker compose --parallel 1 build personnel-service
docker compose up -d --no-deps --force-recreate personnel-service
```

Après une modification de la configuration Docker ou du fichier `.env`,
recréez la pile sans forcer de build :

```bash
docker compose up -d
```

Les journaux déjà existants ne sont pas réduits rétroactivement. Sur un disque
presque plein, vérifiez d'abord `docker system df`; ne nettoyez le cache de
build (`docker builder prune`) qu'après avoir validé les éléments à supprimer.

## Déploiement de production

Le fichier `docker-compose.prod.yml` complète la configuration locale. Il ferme
tous les ports techniques : seuls l'API Gateway (`127.0.0.1:8888`) et le
portail (`127.0.0.1:3140`) sont accessibles depuis l'hôte, pour Nginx.

```text
api-hopital.exemple.cd → Nginx → API Gateway → services internes
gestion.exemple.cd     → Nginx → portail Next.js
```

Avant le premier déploiement, créez un enregistrement DNS pour le domaine vers
l'adresse IP du serveur, puis préparez les secrets. Les valeurs contenant `$`
doivent être placées entre guillemets simples, sinon Docker Compose tente de
les interpréter comme des variables.

```bash
cp .env.production.example .env
# Éditez .env : tous les mots de passe.
docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

Sur un VPS contraint, remplacez la dernière commande par une construction
séquentielle des seuls services modifiés, puis lancez `up -d` sans `--build`.

Installez ensuite le fichier
`infrastructure/proxy/nginx/hopital-gestion.conf` dans Nginx, en y remplaçant
les deux domaines d'exemple. Nginx garde les ports publics `80` et `443` ainsi
que la gestion des certificats TLS. Grafana et Prometheus sont privés par
défaut ; leurs ports peuvent être publiés explicitement via les variables
`GRAFANA_BIND_ADDRESS`, `GRAFANA_HOST_PORT`, `PROMETHEUS_BIND_ADDRESS` et
`PROMETHEUS_HOST_PORT`. Par exemple, `GRAFANA_BIND_ADDRESS=0.0.0.0` et
`GRAFANA_HOST_PORT=13000` publient Grafana sur le port VPS `13000`.

Sur un serveur Ubuntu équipé de Nginx, activez le virtual host après avoir
remplacé les domaines :

```bash
cp infrastructure/proxy/nginx/hopital-gestion.conf /etc/nginx/sites-available/hopital-gestion
ln -s /etc/nginx/sites-available/hopital-gestion /etc/nginx/sites-enabled/hopital-gestion
nginx -t && systemctl reload nginx
certbot --nginx -d gestion.exemple.cd -d api-hopital.exemple.cd
```

Ports utiles :

| Service | URL / port | Usage |
| --- | --- | --- |
| API Gateway Spring | `http://localhost:8888` | Point d'entrée API unique |
| Kong, Keycloak, Kafka et microservices | réseau Docker interne | Services techniques et métier non exposés |
| PostgreSQL | `127.0.0.1:54320` | Administration locale et tunnel SSH uniquement |
| Redis | réseau Docker interne | Cache et rate limiting des services |
| Prometheus | `http://localhost:9090` | Métriques |
| Grafana | `http://localhost:3000` | Dashboards |

### Accès PostgreSQL avec DBeaver

PostgreSQL est lié à `127.0.0.1:${POSTGRES_HOST_PORT:-54320}` : il est
accessible depuis le VPS mais pas depuis Internet. Dans DBeaver, créez une
connexion **PostgreSQL** avec `127.0.0.1`, le port `54320`, l'utilisateur
`hospital` et le mot de passe `HOSPITAL_DB_PASSWORD`. Dans l'onglet **SSH**,
activez le tunnel et indiquez l'adresse, le port SSH et l'utilisateur du VPS.
Vous pouvez alors choisir l'une des bases : `hospital_account`, `hospital_auth`,
`hospital_organization`, `hospital_laboratory`, `hospital_patient`,
`hospital_personnel`, `hospital_pharmacy` ou `hospital_accounting`.

L'alternative équivalente en ligne de commande est :

```bash
ssh -N -L 54320:127.0.0.1:54320 utilisateur@votre-vps
```

Puis connectez DBeaver à `127.0.0.1:54320`. Ne créez pas de règle de pare-feu
pour ce port et ne remplacez pas l'adresse de liaison par `0.0.0.0`.

## Convention de structure des services

Chaque microservice Java doit reprendre cette structure :

```text
src/main/java/.../
  api/                  Controllers REST
  application/
    config/             Configuration applicative
    domain/             Modèle métier
    dto/                Objets d'entrée/sortie
    exception/          Exceptions métier
    service/            Cas d'usage
  infra/
    cache/              Cache Redis
    config/             Configuration technique
    integration/        Clients externes
    security/           Sécurité et JWT
Dockerfile
README.md
```

## Migrations de base de données

Chaque service qui possède une base de données conserve ses migrations SQL dans
`src/main/resources/db/migration`. Les fichiers suivent le format
`V<version>_<nom-bref-de-la-migration>.sql`, par exemple
`V1_create_accounts.sql`. Le séparateur `_` est configuré explicitement dans
Flyway. Les versions sont strictement croissantes et une
migration déjà appliquée ne doit jamais être modifiée.

Toutes les clés primaires et étrangères applicatives utilisent des UUID,
représentés par le type PostgreSQL `UUID` et par `UUID` dans les services Java.
`flyway_schema_history.installed_rank` reste numérique, car il est le compteur
technique de Flyway et non une donnée métier.

Les services `services/account`, `services/auth`, `services/organization`,
`services/laboratory`, `services/patient`, `services/personnel`,
`services/pharmacy` et `services/accounting` appliquent déjà cette convention et servent de modèles
pour les futurs services hospitaliers comme `patient-service`,
`appointment-service`, `staff-service`, `billing-service` ou
`notification-service`.

## Observabilité

Prometheus collecte Kong, Keycloak, `api-gateway`, `auth-service`,
`account-service`, `notification-service`, `organization-service`,
`laboratory-service`, `patient-service`, `pharmacy-service`, `personnel-service`
et `accounting-service` depuis
`infrastructure/monitoring/prometheus/prometheus.yml`. Chaque service Spring
Boot expose `/actuator/prometheus` grâce à Actuator et Micrometer, avec le tag
`application` pour distinguer ses métriques. Grafana provisionne la datasource
Prometheus et le dashboard **Hospital services overview**, qui affiche l'état
des cibles surveillées, le débit HTTP et la mémoire JVM par service.

## Documentation API et tests manuels

La documentation initiale se trouve dans `docs/api`. Les collections Postman
sont séparées par service dans `postman/collections` et partagent
l'environnement local `postman/environments/local.postman_environment.json`.

## Authentification

`account-service` reste la source de vérité des comptes et mots de passe. À
chaque connexion, `auth-service` valide le compte, le crée ou le synchronise
dans Keycloak si nécessaire, aligne ses rôles, puis demande les jetons. Le
contrat de connexion retourne `accessToken`, `refreshToken`, `tokenType`,
`expiresIn`, `expiresAt`, `refreshExpiresIn` et `refreshExpiresAt`.

Les durées locales sont de 15 minutes pour l'access token et 8 heures pour le
refresh token. Chaque connexion est journalisée avec le `User-Agent` HTTP dans
la base `hospital_auth`. Consultez [la documentation Auth](docs/api/auth.md)
pour les corps de requête et réponses.

## Notifications asynchrones

Le topic Kafka `hospital.notification.request.v1` est la file d'attente des
notifications. Un service métier publie un message avec `sourceService`,
`type`, `channels`, `recipients`, `subject`, `body` et `metadata`, puis
`notification-service` se charge de l'envoi e-mail ou SMS hors du chemin HTTP.
Par exemple, `account-service` publie un e-mail de bienvenue après la création
confirmée d'un compte. L'endpoint public
`POST /api/v1/notifications/broadcasts` renvoie `202 Accepted` puis suit le
même flux Kafka.

## Référentiel provincial

Le premier lot métier du système provincial est `organization-service`. Il
structure les référentiels **province → zone de santé → hôpital public →
laboratoire interne** et **province → laboratoire de référence → service, unité
ou département**, puis expose ses opérations via l'API Gateway sous
`/api/v1/organizations`. Une demande d’analyse créée depuis un passage patient
peut être traitée en interne ou référée vers un laboratoire provincial :
prélèvement, code de contenant, expédition, réception ou refus motivé, résultat
et validation sont tous historisés. Le registre des
patients est désormais porté par `patient-service` : il rattache chaque dossier
à son hôpital d'enregistrement et fournit les patients sélectionnables lors
d'une demande d'analyse. `personnel-service` gère maintenant les fiches,
comptes associés, documents et affectations historisées des agents, y compris
l’affectation à un laboratoire de référence. La protection par permissions et
périmètres d’affectation est appliquée par les services ; l’interface ne fait
que masquer les actions inutiles.

## Comptabilité hospitalière

`accounting-service` conserve les factures, encaissements, caisses, écritures
en partie double, journaux, balances, états financiers et annexes par hôpital.
Le plan comptable initial suit un socle SYSCOHADA configurable ; les écritures
validées sont immuables et une correction est toujours matérialisée par une
contrepassation. Les délivrances de la pharmacie alimentent automatiquement la
facturation, l'encaissement et les écritures, avec une reprise idempotente si
la comptabilité est temporairement indisponible. Consultez
[la documentation Comptabilité](docs/api/accounting.md) pour le détail des
rôles, du périmètre et des routes.
