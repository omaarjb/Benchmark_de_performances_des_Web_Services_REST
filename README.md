# 🧾 Benchmark de performances des Web Services REST
> Comparaison des performances entre trois implémentations REST : **JAX-RS (Jersey)**, **Spring MVC** et **Spring Data REST**, à l’aide de **JMeter**, **Prometheus**, **Grafana**, **InfluxDB** et **Docker**.

Ce projet a pour objectif d’évaluer les performances et la consommation de ressources de différentes approches REST Java lors de scénarios de charge lourde simulés avec **Apache JMeter**.

---

## 🏷️ 1. Introduction

Les frameworks REST testés :
- **A-Jersey** : implémentation JAX-RS avec Grizzly HTTP Server
- **C-SpringMVC** : implémentation Spring Boot classique avec contrôleurs MVC
- **D-DataREST** : implémentation Spring Data REST exposant les entités automatiquement

Les métriques sont collectées en temps réel via **Micrometer** et exportées vers **Prometheus**.  
Les tableaux de bord **Grafana** permettent d’analyser la charge CPU, l’utilisation mémoire, la latence GC, et le nombre de threads actifs.

---

## ⚙️ 2. Configuration matérielle et logicielle

| Élément | Valeur |
|----------|--------|
| Machine (CPU, cœurs, RAM) | 12 / 6 / 16 Go |
| OS / Kernel | Windows 11 |
| Java version | 17 |
| Docker / Compose versions | 28.5.1 |
| PostgreSQL version | 16 |
| JMeter version | 5.6.3 |
| Prometheus / Grafana / InfluxDB | 3.7.3 |
| JVM flags (Xms/Xmx, GC) | -Xms2G -Xmx4G G1GC |
| HikariCP (min/max/timeout) | 5 / 20 / 30000 |

## 🧩 3. Structure du projet

L’organisation du projet est modulaire pour séparer les différentes implémentations et les ressources communes.

Benchmark/
│
├── A-jersey/ # Implémentation JAX-RS (Jersey + Grizzly)
├── C-springmvc/ # Implémentation Spring MVC
├── D-datarest/ # Implémentation Spring Data REST
├── common/ # Entités JPA et configuration partagée
├── jmeter-tests/ # Plans de test (.jmx)
├── results/ # Données exportées / graphiques
│
├── docker-compose.yml # Stack de monitoring
├── prometheus.yml # Configuration Prometheus
├── pom.xml # Projet parent Maven
└── jmeter.log # Logs de test

📸 **Capture :**
<img width="458" height="523" alt="image" src="https://github.com/user-attachments/assets/83a5596a-2b5a-4b76-bd90-702bfd2a415f" />


---

## 🐳 4. Conteneurs Docker

Le monitoring du benchmark est entièrement orchestré via Docker Compose.

**Services :**
- 🗄️ **PostgreSQL** : base de données principale  
- 🧰 **pgAdmin** : interface d’administration PostgreSQL  
- 📈 **Prometheus** : collecte des métriques Micrometer  
- 📊 **Grafana** : visualisation des métriques Prometheus et InfluxDB  
- 🧮 **InfluxDB** : stockage des résultats JMeter en temps réel

📸 **Capture :**
<img width="624" height="112" alt="image" src="https://github.com/user-attachments/assets/3e97aaa4-9e3e-42a5-a247-73ae3b9a7d0c" />

---

## 📡 5. Configuration de Prometheus

Le fichier `prometheus.yml` configure la collecte des métriques de chaque service REST :

```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: 'jaxrs'
    metrics_path: '/metrics'
    static_configs:
      - targets: ['host.docker.internal:8081']

  - job_name: 'springmvc'
    metrics_path: '/api/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8082']

  - job_name: 'datarest'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8083']



