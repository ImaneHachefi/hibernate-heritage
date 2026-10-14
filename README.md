# Hibernate / JPA : stratégies d'héritage (SINGLE_TABLE, JOINED, TABLE_PER_CLASS)

Démonstration des trois stratégies d'héritage JPA avec Hibernate et une base de données H2 en mode fichier.

## Sommaire
1. [Présentation](#1-présentation)
2. [Structure du projet](#2-structure-du-projet)
3. [Prérequis et configuration](#3-prérequis-et-configuration)
4. [Connexion à la base H2](#4-connexion-à-la-base-h2)
5. [Exécution de l'application](#5-exécution-de-lapplication)
6. [Vérification SQL](#6-vérification-sql)
7. [Comparaison des stratégies](#7-comparaison-des-stratégies)
8. [Auteur](#8-auteur)

---

## 1. Présentation

| Stratégie | Package | Hiérarchie | Tables générées |
|---|---|---|---|
| SINGLE_TABLE | `main.model.singletable` | Vehicule > Voiture, Moto | `VEHICULE` (colonne `vehicule_type`) |
| JOINED | `main.model.joined` | Employe > Developpeur, Manager | `EMPLOYE`, `DEVELOPPEUR`, `MANAGER` |
| TABLE_PER_CLASS | `main.model.tableperclass` | Produit > Livre, Electronique | `LIVRE`, `ELECTRONIQUE` |

## 2. Structure du projet

```
src/main/
├── java/main/
│   ├── App.java
│   └── model/
│       ├── singletable/
│       ├── joined/
│       └── tableperclass/
└── resources/META-INF/persistence.xml
```
![img_1.png](img_1.png)
*Figure 1 : arborescence du projet dans l'IDE*

## 3. Prérequis et configuration

- Java 8+
- Maven
- Dépendances : `hibernate-core` 5.x, `h2`, `javax.persistence-api`
- Unité de persistance : `hibernate-inheritance`
- `hibernate.hbm2ddl.auto=create` : les tables sont recréées à chaque exécution

## 4. Connexion à la base H2

| Paramètre | Valeur |
|---|---|
| URL JDBC | `jdbc:h2:file:./data/heritage_db;AUTO_SERVER=TRUE` |
| Utilisateur | `sa` |
| Mot de passe | *(vide)* |
| Driver | `org.h2.Driver` |

![img_4.png](img_4.png)

*Figure 2 : écran de connexion de la console H2*

![img_8.png](img_8.png)
![img_9.png](img_9.png)

*Figure 3 : tables générées automatiquement par Hibernate*

![img_5.png](img_5.png)
![img_6.png](img_6.png)

## 5. Exécution de l'application

```bash
mvn clean compile exec:java -Dexec.mainClass=main.App
```

La classe `main.App` insère des données, les relit via des requêtes polymorphiques, modifie un véhicule et un manager, supprime un livre, puis réaffiche tout.

![img_10.png](img_10.png)
![img_11.png](img_11.png)
![img_12.png](img_12.png)
![img_13.png](img_13.png)
![img_14.png](img_14.png)

*Figure 4 : sortie console de l'application (état initial)*

![img_15.png](img_15.png)
![img_16.png](img_16.png)
![img_17.png](img_17.png)
![img_18.png](img_18.png)

*Figure 5 : sortie console après mise à jour et suppression*

## 6. Vérification SQL

### 6.1 SINGLE_TABLE

```sql
SELECT * FROM VEHICULE;
```
![img_19.png](img_19.png)
*Figure 6 : tous les véhicules dans une seule table*

```sql
SELECT VEHICULE_TYPE, COUNT(*) AS total FROM VEHICULE GROUP BY VEHICULE_TYPE;
```
![img_20.png](img_20.png)
*Figure 7 : répartition par colonne discriminante*

### 6.2 JOINED

```sql
SELECT * FROM EMPLOYE;
SELECT * FROM DEVELOPPEUR;
SELECT * FROM MANAGER;
```
![img_21.png](img_21.png)
*Figure 8 : les trois tables de la stratégie JOINED*

```sql
SELECT e.ID, e.NOM, d.LANGAGE
FROM EMPLOYE e JOIN DEVELOPPEUR d ON d.ID = e.ID;

SELECT e.ID, e.NOM, m.NOMBREEQUIPES
FROM EMPLOYE e JOIN MANAGER m ON m.ID = e.ID;
```
![img_22.png](img_22.png)
*Figure 9 : jointures entre la table parente et les sous-classes*

### 6.3 TABLE_PER_CLASS

```sql
SELECT * FROM LIVRE;
SELECT * FROM ELECTRONIQUE;
```
![img_23.png](img_23.png)
*Figure 10 : une table par classe concrète (pas de table PRODUIT)*

```sql
SELECT ID, NOM, 'LIVRE' AS TYPE FROM LIVRE
UNION ALL
SELECT ID, NOM, 'ELECTRONIQUE' FROM ELECTRONIQUE;
```
![img_24.png](img_24.png)
*Figure 11 : requête polymorphique simulée avec UNION ALL*

## 7. Comparaison des stratégies

| Critère | SINGLE_TABLE | JOINED | TABLE_PER_CLASS |
|---|---|---|---|
| Nombre de tables | 1 | 1 par classe | 1 par classe concrète |
| Jointures | Non | Oui | Non (UNION en polymorphique) |
| Colonnes nulles | Nombreuses | Aucune | Aucune |
| Redondance | Faible | Faible | Colonnes communes dupliquées |
| Performance | Très bonne | Moyenne | Moyenne (polymorphisme) |
| Génération d'id | IDENTITY possible | IDENTITY possible | `AUTO` obligatoire |
