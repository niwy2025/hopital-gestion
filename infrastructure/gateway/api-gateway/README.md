# API Gateway Spring Boot

Gateway interne basé sur Spring Cloud Gateway. Kong reste le point d'entrée
public, puis route vers ce gateway ou directement vers certains services selon
la stratégie retenue.

## Structure

- `api` : endpoints techniques du gateway.
- `config` : configuration transverse, dont le rate limiting.
- `resources/application.yml` : routes, ports et exposition Prometheus.
