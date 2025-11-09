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
<img width="1605" height="470" alt="image" src="https://github.com/user-attachments/assets/6e3a300a-b064-489a-9bdc-4cdbb6b7323f" />

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
```

📊 6. InfluxDB & Grafana
InfluxDB est utilisé comme base de données temporelle pour stocker les résultats des tests JMeter via le Backend Listener.

Grafana récupère les données de Prometheus (métriques JVM) et de InfluxDB (résultats JMeter) pour visualiser les performances.

📸 **Capture :**
<img width="1917" height="1041" alt="image" src="https://github.com/user-attachments/assets/37fa79db-71d6-4643-8489-3742f5b67a0e" />
📸 **Capture :**
<img width="1912" height="1032" alt="image" src="https://github.com/user-attachments/assets/0fe09747-d9cb-4f49-a420-79ee8fc7f745" />

## 🧪 7. Scénarios JMeter

Les tests de performance ont été réalisés à l’aide d’**Apache JMeter (v5.6.3)**, afin de simuler différents types de charge sur les endpoints REST exposés par les trois implémentations (**JAX-RS**, **Spring MVC**, **Spring Data REST**).

Deux scénarios principaux ont été définis pour représenter des profils d’utilisation distincts : un test de **lecture intensive** et un test de **corps de requêtes volumineux**.

---

### 📘 Scénario 1 — ReadHeavy (Lecture intensive)

Ce scénario évalue la performance du système lors de fortes charges de **requêtes GET**.  
L’objectif est de mesurer la capacité du serveur à répondre à des lectures simultanées et répétées.

**Paramètres du Thread Group :**
- **Nombre d’utilisateurs (threads)** : 100  
- **Ramp-up period** : 60 secondes  
- **Durée totale du test** : 600 secondes  
- **Type de requêtes** : `GET` sur plusieurs endpoints (items et catégories)
- **Répétition** : Infinie (jusqu’à la fin du temps défini)
- **Backend Listener** : Envoi des métriques vers **InfluxDB**

**Endpoints testés :**
- `GET /items?page=&size=`
- `GET /items?categoryId=&page=&size=`
- `GET /categories/{id}/items?page=&size=`
- `GET /categories?page=&size=`

📸 **Capture :**
<img width="624" height="300" alt="image" src="https://github.com/user-attachments/assets/5a30ade5-169b-49c8-a61a-95d67735bb38" />

### 📕 Scénario 2 — HeavyBody (Écriture intensive)

Ce scénario met l’accent sur les opérations d’écriture impliquant des **corps de requêtes JSON volumineux**.  
L’objectif est de mesurer la consommation CPU et mémoire lors de traitements plus lourds (POST/PUT).

**Paramètres du Thread Group :**
- **Nombre d’utilisateurs (threads)** : 60  
- **Ramp-up period** : 0 seconde  
- **Durée totale du test** : 480 secondes  
- **Type de requêtes** : `POST` et `PUT` sur les ressources `/items`  
**Endpoints testés :** 
  - 50% `POST /items (5 KB)`  
  - 50% `PUT /items/{id} (5 KB)`
- **Backend Listener** : Envoi vers InfluxDB  
- **Gestionnaires :** HTTP Cache / Cookie / Header Managers activés

📸 **Capture :**
<img width="1217" height="687" alt="image" src="https://github.com/user-attachments/assets/1cfc2091-ac20-4248-82a6-ddbb3bcb5c63" />


## 🔁 Configuration du Backend Listener (JMeter → InfluxDB)
📸 **Capture :**
<img width="885" height="460" alt="image" src="https://github.com/user-attachments/assets/52c5703b-5f1c-46aa-9070-9f47627091f2" />

## 📊 8. Résultats JMeter (par scénario et framework)

Les tests de charge ont été exécutés avec **Apache JMeter 5.6.3**, en utilisant :
- **Scénario ReadHeavy** : 100 utilisateurs simultanés sur 600 secondes  
- **Scénario HeavyBody** : 60 utilisateurs simultanés sur 480 secondes

Chaque scénario a été reproduit pour les trois implémentations :
- 🅰️ **JAX-RS (Jersey)**  
- 🅱️ **Spring MVC (@RestController)**  
- 🅾️ **Spring Data REST**

Les métriques ont été enregistrées en temps réel dans **InfluxDB**, puis visualisées via **Grafana** (dashboards personnalisés : RPS, latence p50/p95/p99 et taux d’erreur).

---

### 📘 Scénario 1 — ReadHeavy (100 utilisateurs, durée 600 s)

Ce scénario simule des charges intensives de lecture (`GET`) sur les endpoints principaux :  
`/items`, `/categories`, `/categories/{id}/items`.

#### ⚙️ Paramètres :
- **Threads (utilisateurs)** : 100  
- **Ramp-up** : 60 s  
- **Durée** : 600 s  
- **Boucle** : infinie jusqu’à expiration  
- **Backend Listener** : InfluxDB

#### 📋 Résultats :

| Scénario | Mesure | A : Jersey | C : @RestController | D : Spring Data REST |
|-----------|---------|------------|---------------------|----------------------|
| READ-heavy | RPS | **2.15K req/s** | 1.70K req/s | 1.16K req/s |
| READ-heavy | p50 (ms) | **7.63** | 53.3 | 190 |
| READ-heavy | p95 (ms) | **24.7** | 95.8 | 285 |
| READ-heavy | p99 (ms) | **34.9** | 112 | 372 |
| READ-heavy | Err % | 0 | 0 | 0 |


📸 **Captures Grafana :**

#### 🅰️ Jersey  
<img width="975" height="527" alt="image" src="https://github.com/user-attachments/assets/85b57adc-9a7a-4353-be40-404b555a70ec" />

#### 🅱️ Spring MVC  
<img width="975" height="527" alt="image" src="https://github.com/user-attachments/assets/fe8bf100-746f-47b7-a1e0-075044fcba3e" />

#### 🅾️ Spring Data REST  
<img width="975" height="527" alt="image" src="https://github.com/user-attachments/assets/f3816b66-96cd-49bb-9fe0-896f6c0312fa" />

---

#### 🧩 Analyse :
- **Jersey (JAX-RS)** est de loin le plus performant avec **2.15K req/s** et des latences très faibles (< 40 ms).  
- **Spring MVC** affiche une latence p95 quatre fois supérieure, mais reste stable.  
- **Spring Data REST** est le plus lent, environ **1.16K req/s**, en raison de la sérialisation plus lourde et de la gestion implicite des entités.  
- Aucun framework n’a produit d’erreurs, démontrant la stabilité du test.

---

### 📕 Scénario 2 — HeavyBody (60 utilisateurs, durée 480 s)

Ce scénario évalue la performance sur des requêtes d’écriture lourdes (`POST` et `PUT`) contenant des corps JSON d’environ **5 Ko**.

#### ⚙️ Paramètres :
- **Threads (utilisateurs)** : 60  
- **Ramp-up** : 0 s  
- **Durée** : 480 s  
- **Corps JSON** : 5 KB  
- **Répartition** : 50% POST /items, 50% PUT /items/{id}

#### 📋 Résultats :

| Scénario | Mesure | A : Jersey | C : @RestController | D : Spring Data REST |
|-----------|---------|------------|---------------------|----------------------|
| HEAVY-body | RPS | **969 req/s** | 950 req/s | 948 req/s |
| HEAVY-body | p50 (ms) | **7.52** | 13.5 | 14.0 |
| HEAVY-body | p95 (ms) | **11.9** | 21.6 | 20.7 |
| HEAVY-body | p99 (ms) | **13.0** | 23.3 | 22.0 |
| HEAVY-body | Err % | 0.0408 | 0.0252 | 0.0352 |

📸 **Captures Grafana :**

#### 🅰️ Jersey  
<img width="975" height="527" alt="image" src="https://github.com/user-attachments/assets/bd9397ec-f8ef-42f9-94dd-806be8e9c5fd" />

#### 🅱️ Spring MVC  
<img width="975" height="527" alt="image" src="https://github.com/user-attachments/assets/d7c78264-72f3-4e24-ac19-1d74519c5ca7" />

#### 🅾️ Spring Data REST  
<img width="975" height="528" alt="image" src="https://github.com/user-attachments/assets/a7d27adf-e979-4f20-a6ce-20818efb1cec" />

---

#### 🧩 Analyse :
- Les trois frameworks présentent des performances **très proches** en écriture lourde (~950 req/s).  
- **Jersey** reste légèrement en tête sur les latences (p50 = 7.5 ms).  
- **Spring MVC** et **Spring Data REST** affichent des résultats quasi identiques, mais avec une consommation CPU légèrement supérieure.  
- Les taux d’erreurs restent très faibles (< 0.05%), validant la stabilité générale du test sous contrainte.

---

### 🧠 Synthèse comparative

| Framework | Type dominant | Débit global | Latence moyenne | Efficacité CPU/mémoire |
|------------|----------------|------------------|------------------|-----------------------|
| **JAX-RS (Jersey)** | Lecture / Écriture | 🥇 Excellent (2.15K / 969 req/s) | Très faible | Très efficace |
| **Spring MVC** | Équilibré | 🥈 Bon (1.70K / 950 req/s) | Moyenne | Bonne stabilité |
| **Spring Data REST** | Simplicité | 🥉 Correct (1.16K / 948 req/s) | Plus élevée | Consommation importante |

---

📈 **Observation générale :**
- Les performances en lecture sont clairement dominées par **Jersey**, grâce à sa légèreté et son faible overhead.  
- **Spring MVC** offre un bon compromis entre performance et flexibilité.  
- **Spring Data REST** est plus pratique, mais sa couche abstraite ajoute une surcharge mesurable.  

---

### 📊 Tableau complet — Résultats JMeter (par scénario et variante)

| Scénario | Mesure | A : Jersey | C : @RestController | D : Spring Data REST |
|-----------|---------|------------|---------------------|----------------------|
| **READ-heavy** | RPS | **2.15K req/s** | 1.70K req/s | 1.16K req/s |
|  | p50 (ms) | **7.63** | 53.3 | 190 |
|  | p95 (ms) | **24.7** | 95.8 | 285 |
|  | p99 (ms) | **34.9** | 112 | 372 |
|  | Err % | 0 | 0 | 0 |
| **JOIN-filter** | RPS |  |  |  |
|  | p50 (ms) |  |  |  |
|  | p95 (ms) |  |  |  |
|  | p99 (ms) |  |  |  |
|  | Err % |  |  |  |
| **MIXED (2 entités)** | RPS |  |  |  |
|  | p50 (ms) |  |  |  |
|  | p95 (ms) |  |  |  |
|  | p99 (ms) |  |  |  |
|  | Err % |  |  |  |
| **HEAVY-body** | RPS | **969 req/s** | 950 req/s | 948 req/s |
|  | p50 (ms) | **7.52** | 13.5 | 14.0 |
|  | p95 (ms) | **11.9** | 21.6 | 20.7 |
|  | p99 (ms) | **13.0** | 23.3 | 22.0 |
|  | Err % | 0.0408 | 0.0252 | 0.0352 |

---





  




