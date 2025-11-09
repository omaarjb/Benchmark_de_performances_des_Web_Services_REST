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

L'organisation du projet est modulaire pour séparer les différentes implémentations et les ressources communes.
```
Benchmark/
│
├── A-jersey/            # Implémentation JAX-RS (Jersey + Grizzly)
├── C-springmvc/         # Implémentation Spring MVC
├── D-datarest/          # Implémentation Spring Data REST
├── common/              # Entités JPA et configuration partagée
├── jmeter-tests/        # Plans de test (.jmx)
├── results/             # Données exportées / graphiques
│
├── docker-compose.yml   # Stack de monitoring
├── prometheus.yml       # Configuration Prometheus
├── pom.xml              # Projet parent Maven
└── jmeter.log           # Logs de test
```

📸 **Capture :**
<img width="457" height="642" alt="image" src="https://github.com/user-attachments/assets/383b0147-ea44-470f-9d52-bcea6c1f9a8e" />



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
<img width="1212" height="677" alt="image" src="https://github.com/user-attachments/assets/f96bdc78-bc64-4669-86ef-f6a2e3c375a5" />

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

## 🧠 9. Consommation des ressources JVM (Prometheus)

Les tests de charge du scénario **ReadHeavy (100 utilisateurs, 600 s)** ont permis de mesurer l’utilisation des **ressources JVM** (CPU, mémoire, threads, GC, pool de connexions) via **Micrometer / Prometheus / Grafana**.

Les valeurs indiquées représentent les **moyennes (moy)** et **pics observés (pic)** pendant la durée du test.

---

### 📊 Tableau T3 — Ressources JVM (Prometheus)

| Variante | CPU proc. (%) moy/pic | Heap (Mo) moy/pic | GC time (ms/s) moy/pic | Threads actifs moy/pic | Hikari (actifs/max) |
|-----------|-----------------------|-------------------|------------------------|------------------------|---------------------|
| **A : Jersey (JAX-RS)** | **6.57 / 13.5** | **65.8 / 90.1** | **4.35 / 6.07** | **55.0 / 56** | - |
| **C : Spring MVC (@RestController)** | 12 / 22 | 132 / 191 | 2.19 / 3.95 | 91.9 / 106 | 17.8 / 66 |
| **D : Spring Data REST** | 30.6 / 42.2 | 150 / 241 | 7.23 / 9.29 | 97.9 / 107 | 40.1 / 67 |

---

### 📸 Visualisation Grafana — Ressources JVM (ReadHeavy)

#### 🅰️ **Jersey (JAX-RS)**
- **Moyenne :**
  <img width="975" height="528" alt="image" src="https://github.com/user-attachments/assets/25e2e195-5ee7-4e5c-b016-1f391f4f0ba8" />
- **Pic :**
  <img width="975" height="503" alt="image" src="https://github.com/user-attachments/assets/507671ee-b34d-408e-be7c-8c3b1a7b5ff8" />

---

#### 🅱️ **Spring MVC (@RestController)**
- **Moyenne :**
  <img width="975" height="530" alt="image" src="https://github.com/user-attachments/assets/b671884d-246e-4b3b-8595-9c395f2f63b0" />
- **Pic :**
  <img width="975" height="515" alt="image" src="https://github.com/user-attachments/assets/17f4aa28-eea3-4230-aec6-a9f3880ddb2b" />

---

#### 🅾️ **Spring Data REST**
- **Moyenne :**
  <img width="975" height="527" alt="image" src="https://github.com/user-attachments/assets/096a2be1-377e-44ef-b850-3807ce96126a" />
- **Pic :**
  <img width="975" height="527" alt="image" src="https://github.com/user-attachments/assets/877bf027-bb14-4c6c-9a69-f4ee70beca34" />

---

### 🧩 Analyse des résultats

- **CPU :**  
  Jersey est nettement plus léger (≈6.5 % en moyenne), tandis que Spring Data REST monte à plus de 40 % lors des pics.  
  Cela s’explique par le coût supplémentaire des conversions d’entités et de la sérialisation automatique.

- **Mémoire (Heap) :**  
  Spring Data REST consomme le plus de mémoire (≈150–241 Mo), suivi de Spring MVC (≈130–190 Mo).  
  Jersey reste particulièrement efficace avec une utilisation stable (~65–90 Mo).

- **Garbage Collector (GC) :**  
  Spring MVC montre les pauses GC les plus courtes (≈2–4 ms), alors que Spring Data REST connaît des cycles plus longs (jusqu’à 9 ms).  
  Jersey reste équilibré.

- **Threads :**  
  Spring Data REST crée davantage de threads actifs (~100), conséquence de la gestion automatique des couches Spring.  
  Jersey en maintient environ 55, soit près de deux fois moins.

- **HikariCP :**  
  Seules les implémentations Spring utilisent HikariCP.  
  Spring MVC affiche une utilisation raisonnable (~18/66 connexions),  
  tandis que Spring Data REST monte à ~40/67 sous forte charge.

---

### 🧠 Conclusion synthétique

| Critère | Jersey | Spring MVC | Spring Data REST |
|----------|---------|-------------|------------------|
| **Efficacité CPU** | 🥇 Excellente | 🥈 Bonne | 🥉 Moyenne |
| **Mémoire (Heap)** | 🥇 Faible consommation | 🥈 Modérée | 🥉 Élevée |
| **Stabilité GC** | 🥈 Correcte | 🥇 Optimale | 🥉 Moyenne |
| **Threads actifs** | 🥇 Légère | 🥈 Moyenne | 🥉 Lourde |
| **Pool Hikari** | - | 🥇 Contrôlé | 🥈 Chargé |

---

📈 *Ces mesures confirment que l’implémentation JAX-RS (Jersey) reste la plus économe en ressources, tandis que les frameworks Spring offrent plus de confort de développement au prix d’un coût mémoire et CPU supérieur.*

---

## 🧩 10. Détails par endpoint

### 🧠 T4 — Détails par endpoint (scénario JOIN-filter)

| Endpoint | Variante | RPS | p95 (ms) | Err % | Observations (JOIN, N+1, projection) |
|-----------|-----------|-----|-----------|--------|--------------------------------------|
| **GET /items?categoryId=** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |
| **GET /categories/{id}/items** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |

---

### ⚙️ T5 — Détails par endpoint (scénario MIXED)

| Endpoint | Variante | RPS | p95 (ms) | Err % | Observations |
|-----------|-----------|-----|-----------|--------|---------------|
| **GET /items** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |
| **POST /items** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |
| **PUT /items/{id}** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |
| **DELETE /items/{id}** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |
| **GET /categories** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |
| **POST /categories** | A |  |  |  |  |
|  | C |  |  |  |  |
|  | D |  |  |  |  |


## ⚠️ 11. Incidents et erreurs

### 🧾 T6 — Incidents / erreurs

| Run | Variante | Type d’erreur (HTTP / DB / timeout) | % | Cause probable | Action corrective |
|------|-----------|-------------------------------------|---|----------------|-------------------|
|  |  |  |  |  |  |
|  |  |  |  |  |  |
|  |  |  |  |  |  |

---

## 🧩 12. Synthèse & conclusion

### 🧠 T7 — Synthèse & conclusion

| Critère | Meilleure variante | Écart (justifier) | Commentaires |
|----------|--------------------|------------------|---------------|
| **Débit global (RPS)** |  |  |  |
| **Latence p95** |  |  |  |
| **Stabilité (erreurs)** |  |  |  |
| **Empreinte CPU / RAM** |  |  |  |
| **Facilité d’expo relationnelle** |  |  |  |

---


## 🚀 13. Démarrage du benchmark

Cette section décrit comment exécuter le projet de benchmark complet (applications, base de données, monitoring, et tests de charge).

---

### 🧩 Prérequis

Avant de démarrer, assurez-vous d’avoir installé :

- **Docker Desktop** ≥ 28.5.1  
- **Java JDK 17**  
- **Apache Maven** ≥ 3.9  
- **Apache JMeter** ≥ 5.6.3  
- **Git** (pour cloner le projet)
- Optionnel : **pgAdmin** (gestion de la base PostgreSQL)

---

### 🏗️ 1. Cloner le projet

```bash
git clone https://github.com/<votre-utilisateur>/benchmark-rest.git
cd benchmark-rest
```
---

## 🐋 2. Lancer l’infrastructure Docker

Le fichier `docker-compose.yml` se trouve à la racine du projet et déploie les services suivants :

| Service | Description | Port |
|----------|--------------|------|
| **PostgreSQL** | Base de données relationnelle | `5433:5432` |
| **pgAdmin** | Interface web de gestion PostgreSQL | `5050:80` |
| **Prometheus** | Collecte des métriques des microservices | `9090:9090` |
| **Grafana** | Visualisation des dashboards | `3000:3000` |
| **InfluxDB** | Stockage des résultats JMeter | `8086:8086` |

Démarrez tous les conteneurs :

```bash
docker-compose up -d
```

💡 **Vérifiez ensuite que les conteneurs sont bien UP avec :**

```bash
docker ps
```

---

## ⚙️ 3. Démarrer les applications à tester

Chaque module correspond à une variante du web service :

| Module | Framework | Port | Commande de démarrage |
|---------|------------|------|------------------------|
| **A-jersey** | JAX-RS (Grizzly + Jersey) | `8081` | `mvn clean package` → puis `java -jar target/A-jersey.jar` |
| **C-springmvc** | Spring MVC + @RestController | `8082` | `mvn spring-boot:run` |
| **D-datarest** | Spring Data REST | `8083` | `mvn spring-boot:run` |

⚠️ **Assurez-vous que chaque application expose bien ses métriques Prometheus :**

- JAX-RS → `/api/metrics`
- Spring MVC → `/api/actuator/prometheus`
- Spring Data REST → `/actuator/prometheus`

---

## 📡 4. Vérifier la configuration Prometheus

Les endpoints sont définis dans `prometheus.yml` :

```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: 'jaxrs'
    metrics_path: '/api/metrics'
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

🧠 **Accès à Prometheus :** [http://localhost:9090/targets](http://localhost:9090/targets)

Vous devez voir les trois services avec l’état **UP ✅**

---

## 📊 5. Accéder à Grafana

- **URL** : [http://localhost:3000](http://localhost:3000)
- **Identifiants par défaut :**
  ```
  user: admin
  password: admin
  ```

Importez les dashboards :

- **JVM (Micrometer)** → pour suivre CPU, Heap, GC, Threads, Hikari
- **JMeter (InfluxDB)** → pour suivre RPS, latence, erreurs, percentiles

---

## 🧪 6. Lancer les scénarios JMeter

Les fichiers de test se trouvent dans le dossier `/jmeter-tests` :

| Fichier | Description | Utilisateurs | Durée |
|----------|--------------|---------------|--------|
| `ReadHeavy.jmx` | Scénario de lecture intensive | 100 | 600 s |
| `HeavyBody.jmx` | Scénario POST/PUT lourd (body JSON 5 KB) | 60 | 480 s |
| `JoinFilter.jmx` | Scénario de requêtes filtrées (JOIN, N+1) | (à définir) | (à définir) |
| `Mixed.jmx` | Scénario combiné (CRUD mixte) | (à définir) | (à définir) |

Pour exécuter un test et envoyer les résultats vers InfluxDB :

```bash
jmeter -n -t jmeter-tests/ReadHeavy.jmx -l results/readheavy.jtl \
  -e -o results/dashboard \
  -Jinfluxdb.url=http://localhost:8086 \
  -Jinfluxdb.db=jmeter
```

---

## 📈 7. Visualiser les résultats

### 🔹 Dashboard "JMeter + InfluxDB"
Permet de visualiser :

- RPS (Requêtes par seconde)
- p50, p95, p99 (latence)
- Erreurs (%)
- Comparaison entre variantes

### 🔹 Dashboard "JVM (Micrometer)"

- Utilisation CPU / mémoire
- Temps de GC
- Threads actifs
- Connexions HikariCP

---

## 🧹 8. Nettoyer les conteneurs

Pour arrêter et supprimer tous les conteneurs :

```bash
docker-compose down
```

---

## ✅ Résumé

| Étape | Description | Commande clé |
|-------|--------------|---------------|
| 1️⃣ | Démarrer Docker | `docker-compose up -d` |
| 2️⃣ | Lancer les apps | `mvn spring-boot:run` / `java -jar` |
| 3️⃣ | Vérifier Prometheus | `localhost:9090/targets` |
| 4️⃣ | Ouvrir Grafana | `localhost:3000` |
| 5️⃣ | Lancer JMeter | `jmeter -n -t test.jmx -l results.jtl` |

---

💬 Vous pouvez maintenant exécuter vos benchmarks, observer les métriques JVM et comparer les performances entre les trois implémentations REST : **Jersey**, **Spring MVC** et **Spring Data REST**.




  




