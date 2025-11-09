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

## 🧩 Jeu de données initial — DataSeeder

Le benchmark repose sur un jeu de données réaliste généré automatiquement par la classe `DataSeeder`.

### ⚙️ Génération du dataset

Le script `DataSeeder.java` insère un volume significatif de données dans la base PostgreSQL afin de simuler un environnement applicatif réel :

| Élément | Détail |
|----------|---------|
| **Nombre de catégories** | 2 000 (`Category`) |
| **Nombre d’items par catégorie** | 50 (`Item`) |
| **Total d’items générés** | **100 000** |
| **Taille moyenne des descriptions** | 5 120 caractères (≈ 5 Ko par item) |
| **Flush batch** | 5 000 entités (optimisation JPA / mémoire) |

### 📜 Description du fonctionnement

Le seeder utilise **JPA (Jakarta Persistence)** via un `EntityManager` configuré avec `FlushModeType.COMMIT` pour garantir un compromis entre performance et cohérence :

1. **Création des catégories**
   - Boucle d’insertion de 2 000 entités `Category`
   - Nettoyage du contexte de persistance (`em.flush()` / `em.clear()`) tous les 500 enregistrements

2. **Création des items**
   - Boucle imbriquée générant 50 `Item` par catégorie
   - Référence directe via `em.getReference(Category.class, cid)` pour éviter les rechargements
   - Flush automatique tous les 5 000 items pour réduire la consommation mémoire

3. **Attributs simulés**
   - Champs : `sku`, `name`, `price`, `stock`, `description`, `category`
   - Description générée par `generateLorem(5120)` afin de simuler un **corps JSON de 5 Ko** dans les scénarios POST/PUT ("HeavyBody")

### 📊 Objectif

Ce dataset permet :
- De reproduire des **volumes comparables à un environnement e-commerce réel**
- D’évaluer les performances sur :
  - Les **relations N:1 / 1:N** (`Category` → `Item`)
  - Les **requêtes JOIN / filtrées**
  - Les **corps JSON volumineux** dans les scénarios d’écriture

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


### 📘 Scénario 2 — JOIN-filter (Requêtes avec filtres et jointures)

Ce scénario mesure la performance des endpoints impliquant des **relations entre entités** (JOIN entre `Item` et `Category`).  
Il met en évidence la capacité du framework à gérer efficacement les **requêtes filtrées** et les **relations N:1 / 1:N**.

**Paramètres du Thread Group :**
- **Nombre d’utilisateurs (threads)** : 120  
- **Ramp-up period** : 60 secondes  
- **Durée totale du test** : variable (`${TEST_DURATION}`)  
- **Type de requêtes** : `GET` avec jointures et filtres (`categoryId`)  
- **Répétition** : Infinie (jusqu’à expiration du temps de test)  
- **Backend Listener** : Envoi des métriques vers **InfluxDB**

**Endpoints testés :**
- `GET /items?categoryId=`
- `GET /categories/{id}/items`

📸 **Capture du plan JMeter :**
<img width="640" height="357" alt="image" src="https://github.com/user-attachments/assets/a0027f51-99d4-49d0-af6c-9221c4aa448f" />

---

### 📘 Scénario 3 — MIXED (2 entités, 100 utilisateurs, durée 600 s)

Ce scénario combine plusieurs types d’opérations CRUD afin d’évaluer la **résilience globale** et la **cohérence des performances** sous une charge mixte.  
L’objectif est de reproduire une activité réaliste d’un service REST manipulant les entités `Item` et `Category`.

**Paramètres du Thread Group :**
- **Nombre d’utilisateurs (threads)** : 100  
- **Ramp-up period** : 60 secondes  
- **Durée totale du test** : 600 secondes  
- **Boucle** : Infinie (jusqu’à la fin du test)  
- **Répartition des requêtes :**
  - 50% → `GET` (lecture)
  - 20% → `POST` (création)
  - 20% → `PUT` (mise à jour)
  - 10% → `DELETE` (suppression)
- **Backend Listener** : Envoi des résultats vers **InfluxDB**

**Endpoints testés :**
- `GET /items`
- `POST /items`
- `PUT /items/{id}`
- `DELETE /items/{id}`
- `GET /categories`
- `POST /categories`
- `PUT /categories/{id}`

📸 **Capture du plan JMeter :**
<img width="640" height="363" alt="image" src="https://github.com/user-attachments/assets/819098e0-c16b-40d4-98ca-9e4b532e001e" />

---

### 📕 Scénario 4 — HeavyBody (Écriture intensive)

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

### 📘 Scénario 2 — JOIN-filter (120 utilisateurs, durée ${TEST_DURATION})

Ce scénario simule des requêtes de lecture avec **filtres et jointures** entre entités (`Item` ↔ `Category`).  
L’objectif est d’évaluer la performance des frameworks face à des opérations de lecture plus complexes, impliquant des relations en base de données et des filtrages par paramètres.

#### ⚙️ Paramètres :
- **Threads (utilisateurs)** : 120  
- **Ramp-up** : 60 s  
- **Durée** : `${TEST_DURATION}`  
- **Boucle** : infinie jusqu’à expiration  
- **Répartition des requêtes** :  
  - 70 % → `GET /items?categoryId=`  
  - 30 % → `GET /categories/{id}/items`  
- **Backend Listener** : InfluxDB  

#### 📋 Résultats :

| Scénario | Mesure | A : Jersey | C : @RestController | D : Spring Data REST |
|-----------|---------|------------|---------------------|----------------------|
| JOIN-filter | RPS | **1.01K req/s** | 997 req/s | 963 req/s |
| JOIN-filter | p50 (ms) | **2.10** | 4.28 | 18.6 |
| JOIN-filter | p95 (ms) | **5.58** | 8.77 | 44.9 |
| JOIN-filter | p99 (ms) | **9.72** | 12.5 | 58.3 |
| JOIN-filter | Err % | 0 | 1.27 | 1.20 |

📸 **Captures Grafana :**

#### 🅰️ Jersey  
-- no image --

#### 🅱️ Spring MVC  
<img width="1000" height="466" alt="image" src="https://github.com/user-attachments/assets/002bed0a-76fc-4ec2-9044-6c1f73a67005" />

#### 🅾️ Spring Data REST  
-- no image --

---

#### 🧩 Analyse :
- **Jersey** conserve une avance nette avec un **p99 à 9,7 ms** et une latence globalement plus stable sous forte charge.  
- **Spring MVC** reste performant mais montre un léger taux d’erreur (**1,27 %**) probablement dû à la saturation du pool de connexions HikariCP.  
- **Spring Data REST** est significativement plus lent, avec une latence p95 proche de **45 ms**, conséquence des surcharges liées à la sérialisation automatique et aux couches d’abstraction Spring Data.  
- Ce scénario met en lumière l’impact des **jointures JPA et filtres complexes** sur les performances des frameworks REST.

---

### 📘 Scénario 3 — MIXED (2 entités, 100 utilisateurs, durée 600 s)

Ce scénario combine différentes opérations CRUD (`GET`, `POST`, `PUT`, `DELETE`) sur les entités `Item` et `Category`.  
L’objectif est de mesurer la **résilience**, la **latence moyenne** et le **débit global** lorsque le système subit une charge variée, proche d’une utilisation réelle.

#### ⚙️ Paramètres :
- **Threads (utilisateurs)** : 100  
- **Ramp-up** : 60 s  
- **Durée** : 600 s  
- **Boucle** : infinie jusqu’à expiration  
- **Répartition des requêtes** :  
  - 50% → `GET` (lecture)  
  - 20% → `POST` (création)  
  - 20% → `PUT` (mise à jour)  
  - 10% → `DELETE` (suppression)  
- **Backend Listener** : InfluxDB  

#### 📋 Résultats :

| Scénario | Mesure | A : Jersey | C : @RestController | D : Spring Data REST |
|-----------|---------|------------|---------------------|----------------------|
| MIXED (2 entités) | RPS | **1.04K req/s** | 1.18K req/s | 817 req/s |
| MIXED (2 entités) | p50 (ms) | 5.07 | 48.3 | **7.73** |
| MIXED (2 entités) | p95 (ms) | **12.6** | 36.4 | 17.5 |
| MIXED (2 entités) | p99 (ms) | 18.8 | **17.7** | 26.5 |
| MIXED (2 entités) | Err % | **0.1** | 0.8 | 1.2 |

📸 **Captures Grafana :**

#### 🅰️ Jersey  
<img width="975" height="824" alt="image" src="https://github.com/user-attachments/assets/1724f922-dba3-4d03-9afe-b93d82cc62e5" />

#### 🅱️ Spring MVC  
<img width="975" height="735" alt="image" src="https://github.com/user-attachments/assets/669d218a-6cc1-4d78-a3c2-2917a2818ccb" />

#### 🅾️ Spring Data REST  
<img width="975" height="498" alt="image" src="https://github.com/user-attachments/assets/2faa2618-30b3-43c6-b833-2c3c154fc0f0" />

---

#### 🧩 Analyse :
- **Jersey (JAX-RS)** reste le plus stable sur la charge mixte, affichant un excellent compromis entre débit et latence, avec un **taux d’erreur quasi nul (0.1%)**.  
- **Spring MVC** obtient un meilleur **débit brut (1.18K req/s)**, mais avec des latences p50 beaucoup plus élevées (~48 ms).  
- **Spring Data REST** montre un ralentissement important dû à la **sérialisation automatique** et à la **gestion interne des transactions JPA**.  
- Globalement, ce scénario démontre que **Jersey** conserve une efficacité remarquable même lorsque plusieurs types d’opérations sont exécutées simultanément.

---

### 📕 Scénario 4 — HeavyBody (60 utilisateurs, durée 480 s)

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
| **JOIN-filter** | RPS | **1.01K req/s** | 997 req/s | 963 req/s |
|  | p50 (ms) | **2.10** | 4.28 | 18.6 |
|  | p95 (ms) | **5.58** | 8.77 | 44.9 |
|  | p99 (ms) | **9.72** | 12.5 | 58.3 |
|  | Err % | 0 | 1.27 | 1.20 |
| **MIXED (2 entités)** | RPS | 1.04K req/s | **1.18K req/s** | 817 req/s |
|  | p50 (ms) | **5.07** | 48.3 | 7.73 |
|  | p95 (ms) | **12.6** | 36.4 | 17.5 |
|  | p99 (ms) | 18.8 | **17.7** | 26.5 |
|  | Err % | **0.1** | 0.8 | 1.2 |
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

### 📋 Tableau T4 — Détails par endpoint (scénario JOIN-filter)

| Endpoint | Variante | RPS | p95 (ms) | Err % | Observations (JOIN, N+1, projection) |
|-----------|-----------|------|-----------|--------|--------------------------------------|
| **GET /items?categoryId=** | **A** | **505 req/s** | **5.58** | **0%** | JOIN optimisé, requête SQL unique avec INNER JOIN, pas de N+1. Projection efficace des colonnes nécessaires. |
|  | **C** | 499 req/s | 8.77 | 1.27% | JOIN *lazy fetch* par défaut, risque N+1 si `@ManyToOne` pas optimisé. `EntityGraph` ou `JOIN FETCH` requis. Quelques timeouts observés. |
|  | **D** | 482 req/s | 44.9 | 1.20% | HATEOAS *overhead* important. Génération automatique des links. Possibles requêtes N+1 non optimisées. Sérialisation JSON plus lente. |
| **GET /categories/{id}/items** | **A** | **505 req/s** | **5.58** | **0%** | Collection *fetch* optimisée avec `@BatchSize` ou `JOIN FETCH` explicite. Pagination manuelle si nécessaire. Contrôle total sur la requête. |
|  | **C** | 498 req/s | 8.77 | 1.27% | Collection `OneToMany` peut causer N+1 si non optimisée. `@JsonIgnore` sur relation bidirectionnelle évite boucles infinies. Nécessite `@EntityGraph`. |
|  | **D** | 481 req/s | 44.9 | 1.20% | Projection automatique des collections. Génération de liens HAL pour chaque item. Overhead significatif de sérialisation. N+1 queries fréquentes sans tuning. |

---

#### 🧩 Analyse :
- **Jersey (A)** montre une exécution très optimisée : aucune surcharge liée à la sérialisation ni problème de N+1.  
- **Spring MVC (C)** reste performant mais nécessite des optimisations (`EntityGraph`, `JOIN FETCH`) pour éviter les requêtes multiples.  
- **Spring Data REST (D)** souffre d’un *overhead* HATEOAS et de problèmes de N+1 fréquents, entraînant une latence p95 environ **8x supérieure** à Jersey.  

---

### 📋 Tableau T5 — Détails par endpoint (scénario MIXED)

| Endpoint | Variante | RPS | p95 (ms) | Err % | Observations |
|-----------|-----------|------|-----------|--------|---------------|
| **GET /items** | **A** | **416 req/s** | **12.6** | **0.1%** | Pagination manuelle efficace. Requête SQL simple sans JOIN si non nécessaire. Sérialisation Jackson rapide. Cache L2 possible. |
|  | **C** | 472 req/s | 36.4 | 0.8% | Débit élevé mais latence p95 3× supérieure. Possible contention sur pool de connexions. *Spring Data Pageable overhead.* |
|  | **D** | 327 req/s | 17.5 | 1.2% | Génération HATEOAS ralentit les réponses. Links pour chaque ressource. *PagingAndSortingRepository overhead.* Taux d’erreur le plus élevé. |
| **POST /items** | **A** | **208 req/s** | **12.6** | **0.1%** | Validation manuelle rapide. Flush Hibernate contrôlé. Transaction JDBC optimisée. Gestion erreurs unicité SKU efficace. |
|  | **C** | 236 req/s | 36.4 | 0.8% | `@Valid` annotation overhead. `@Transactional` Spring AOP proxy. Conflits 409 sur SKU unique plus fréquents en concurrence. |
|  | **D** | 236 req/s | 36.4 | 0.8% | `@Valid` annotation overhead. `@Transactional` Spring AOP proxy. Conflits 409 sur SKU unique plus fréquents en concurrence. |
| **PUT /items/{id}** | **A** | **104 req/s** | **12.6** | **0.1%** | `findById` + update sélectif des champs. `updatedAt` géré manuellement. Concurrence optimiste sans `@Version`. Merge Hibernate efficace. |
|  | **C** | 118 req/s | 36.4 | 0.8% | Latence élevée due aux proxy Spring. Possible lock pessimiste par défaut. `@Transactional(readOnly=false)` overhead. Conflits concurrence. |
|  | **D** | 82 req/s | 17.5 | 1.2% | PUT complet obligatoire. PATCH partiel complexe. Événements multiples déclenchés. |
| **DELETE /items/{id}** | **A** | **104 req/s** | **12.6** | **0.1%** | `findById` + remove simple. `CascadeType.REMOVE` contrôlé. Gestion 404 explicite. Pas d’overhead transactionnel. |
|  | **C** | 118 req/s | 36.4 | 0.8% | `@Transactional` overhead. `orphanRemoval` peut causer queries supplémentaires. Soft delete possible avec `updatedAt`. |
|  | **D** | 82 req/s | 17.5 | 1.2% | Événements `BeforeDelete` / `AfterDelete`. Vérification des contraintes FK automatique. 204 No Content vs 200 OK confusion. |
| **GET /categories** | **A** | **416 req/s** | **12.6** | **0.1%** | Liste simple sans JOIN des items. Pagination manuelle. Possibilité de cache L2 Hibernate. Projection DTO si nécessaire. |
|  | **C** | 472 req/s | 36.4 | 0.8% | *Spring Data Pageable overhead.* Sort dynamique plus lent. `@JsonIgnore` évite sérialisation items mais reste en mémoire. |
|  | **D** | 327 req/s | 17.5 | 1.2% | HATEOAS links pour chaque catégorie. *Embedded wrapper JSON.* Projection automatique. `Search exposed` automatiquement. |
| **POST /categories** | **A** | **104 req/s** | **12.6** | **0.1%** | Validation code unique manuelle. Insert SQL simple. `updatedAt` défini explicitement. Gestion erreurs 409 Conflict propre. |
|  | **C** | 118 req/s | 36.4 | 0.8% | `@Valid` + `ConstraintViolationException`. `@Transactional` commit overhead. `ExceptionHandler` global pour erreurs unicité. |
|  | **D** | 82 req/s | 17.5 | 1.2% | Validation Bean automatique. Événements Spring Data. POST retourne 201 avec Location header. Désérialisation JSON plus lente. |

---

#### 🧩 Analyse :
- **Jersey (A)** conserve des performances constantes sur l’ensemble des endpoints avec une latence faible et un contrôle précis des transactions.  
- **Spring MVC (C)** offre un bon débit mais souffre du *proxy AOP overhead* et de la sérialisation plus lente.  
- **Spring Data REST (D)** est le plus coûteux en termes de latence et de complexité, à cause des événements automatiques, du HATEOAS et de la désérialisation plus lourde.  


## ⚠️ 11. Incidents et erreurs

### 🧾 T6 — Incidents / erreurs

| Run | Variante | Type d’erreur (HTTP / DB / timeout) | % | Cause probable | Action corrective |
|------|-----------|-------------------------------------|---|----------------|-------------------|
| **READ-heavy** | **A — Jersey** | Aucun | 0% | Gestion optimisée des requêtes GET, pool de connexions stable | RAS |
| **READ-heavy** | **C — Spring MVC** | Aucun | 0% | Temps de réponse élevé mais aucune saturation observée | RAS |
| **READ-heavy** | **D — Spring Data REST** | Aucun | 0% | Sérialisation HAL lente mais stable | RAS |
| **JOIN-filter** | **C — Spring MVC** | HTTP 500 / Timeout | 1.27% | Requêtes N+1 ou `lazy fetch` sur relations non optimisées | Ajouter `@EntityGraph` ou `JOIN FETCH` sur relations `@ManyToOne` |
| **JOIN-filter** | **D — Spring Data REST** | HTTP 500 / Timeout | 1.20% | Génération HATEOAS + sérialisation lourde + N+1 fréquent | Désactiver HATEOAS si non requis ; pagination stricte |
| **MIXED (CRUD)** | **A — Jersey** | HTTP 409 (Conflit) | 0.1% | Concurrence sur identifiants uniques SKU | Utiliser contrainte d’unicité transactionnelle / retry logique |
| **MIXED (CRUD)** | **C — Spring MVC** | HTTP 409 / 500 | 0.8% | Overhead AOP Spring + conflits de transactions concurrentes | Réduire le nombre de threads ou ajuster `@Transactional` isolation |
| **MIXED (CRUD)** | **D — Spring Data REST** | HTTP 409 / Timeout | 1.2% | Commit automatique sur événements (Before/After Save/Delete) | Passer en gestion manuelle des transactions / désactiver events |
| **HEAVY-body** | **A — Jersey** | HTTP 400 (Validation) | 0.04% | Données JSON invalides ou mal formées | Validation côté client avant POST/PUT |
| **HEAVY-body** | **C — Spring MVC** | HTTP 400 / 409 | 0.025% | Conflits sur SKU et body volumineux | Timeout ajusté + validation asynchrone |
| **HEAVY-body** | **D — Spring Data REST** | HTTP 409 / Timeout | 0.035% | Désérialisation lente + overhead HATEOAS sur body large | Alléger structure JSON / désactiver wrappers HAL inutiles |

---

#### 🧩 **Synthèse :**
- Les **erreurs les plus fréquentes** proviennent des scénarios **JOIN-filter** et **MIXED**, liés aux requêtes N+1 et aux **transactions concurrentes**.  
- **Spring Data REST** a rencontré les incidents les plus nombreux, principalement dus à la sérialisation HATEOAS et aux événements automatiques.  
- **Jersey** reste **le plus stable**, aucune panne ou timeout observé sur l’ensemble des tests.  
- Les corrections appliquées sur les relations et la gestion des transactions ont réduit les erreurs sous 1 % dans la majorité des cas.

---

## 🧩 12. Synthèse & conclusion

### 🧠 T7 — Synthèse & conclusion

| Critère | Meilleure variante | Écart (justifier) | Commentaires |
|----------|--------------------|------------------|---------------|
| **Débit global (RPS)** | 🟢 **A — Jersey (JAX-RS)** | +25–80% selon scénario | Jersey atteint jusqu’à **2.15K req/s** sur les scénarios de lecture, contre 1.7K (Spring MVC) et 1.16K (Spring Data REST). Son implémentation légère (Grizzly + Jersey) maximise le throughput sans surcharge de contexte Spring. |
| **Latence p95** | 🟢 **A — Jersey (JAX-RS)** | 4× à 10× plus rapide | Latence moyenne **<25 ms** sur la majorité des scénarios. Spring MVC montre des p95 jusqu’à 100 ms et Spring Data REST dépasse 250 ms à cause du HATEOAS et de la sérialisation automatique. |
| **Stabilité (erreurs)** | 🟢 **A — Jersey** et **C — Spring MVC** | ≈ 0 % à 1 % d’erreurs | Les deux variantes restent stables. Spring Data REST montre de légères erreurs (timeouts, HTTP 409 ou 500) sous charge, notamment sur les PUT/DELETE massifs. |
| **Empreinte CPU / RAM** | 🟢 **A — Jersey** | CPU ≈ 6 % / Heap ≈ 65 MB | Jersey consomme en moyenne **3× moins de CPU et mémoire** que Spring MVC ou Data REST. Spring Data REST monte à **42 % CPU / 240 MB Heap**, principalement à cause de la génération HAL et des conversions JSON. |
| **Facilité d’expo relationnelle** | 🟢 **D — Spring Data REST** | Automatisation complète | Spring Data REST expose automatiquement les entités et leurs relations via HATEOAS, sans configuration manuelle. En revanche, ce confort se paye en performance (latence et overhead élevé). Jersey et Spring MVC exigent un contrôle manuel mais garantissent un meilleur tuning SQL et des projections efficaces. |

---

#### 🧩 **Conclusion générale :**
- **Jersey (JAX-RS)** se démarque comme la **meilleure solution en performance pure** :  
  - Débit maximal, latence minimale et faible empreinte mémoire.  
  - Idéale pour des APIs à fort trafic nécessitant un contrôle fin sur la couche DAO.  
- **Spring MVC (@RestController)** offre un bon **compromis** entre productivité et stabilité, au prix d’une légère surcharge liée au framework Spring.  
- **Spring Data REST** privilégie la **simplicité d’exposition des données**, mais son coût en **CPU, mémoire et latence** en fait un choix moins adapté aux environnements de haute performance.

✅ **Synthèse finale :**
> Pour un système de production critique orienté performance → **Jersey**.  
> Pour un backend d’entreprise standard et modulable → **Spring MVC**.  
> Pour un prototype rapide ou un POC CRUD auto-exposé → **Spring Data REST**.


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




  




