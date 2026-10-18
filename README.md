# tp1-ms

TP1 : Inversion de Contrôle (IoC) et Injection de Dépendances (DI)

# Compte-Rendu : TP1 - Inversion de Contrôle & Injection de Dépendances

## Contexte Universitaire

- Établissement : Faculté des Sciences de Sfax (FSS)
- Matière : Architecture MicroServices
- Filière : Master Professionnel (MPSRCC1)
- Année Universitaire : 2026-2027
- Enseignantes : Corinne Amel ZAYANI, Amal ABID
- Étudiant(e) : **[VOTRE NOM ET PRÉNOM]**

---

## Objectifs du TP

1. Comprendre et appliquer le concept de **couplage faible** en orienté objet.
2. Comprendre et illustrer l'Inversion de Contrôle et l'Injection de Dépendances (DI).
3. Explorer l'évolution de l'injection de dépendances à travers 4 approches :
   - Activité 1-1 : Instanciation statique manuelle.
   - Activité 1-2 : Instanciation dynamique via fichier de configuration (`config.txt`) et réflexion (`Class.forName`).
   - Activité 1-3 : Utilisation du framework Spring avec configuration XML (`config.xml`).
   - Activité 1-4 : Utilisation des annotations Spring (`@Repository`, `@Service`, `@Autowired`).

---

## Environnement

- JDK 17
- IntelliJ IDEA
- Maven
- Spring Framework 6.1.5

> 📸 **CAPTURE 1 (à placer ici)** : résultat de la commande `java -version` dans le terminal (preuve que JDK 17 est installé).

---

## Introduction & Objectifs de l'Atelier

### Structure du Projet

L'application est organisée en 3 packages afin de garantir une séparation claire des responsabilités (architecture multicouche / MVC) :

- `dao` : regroupe l'abstraction de l'accès aux données (`IDao`) et sa concrétisation (`DaoImpl`).
- `metier` : contient la logique applicative (`IMetier`, `MetierImpl`).
- `presentation` : contient les différentes classes de test illustrant chaque étape.

```
TP1
├── pom.xml
└── src/main
    ├── java
    │   ├── dao
    │   │   ├── IDao.java
    │   │   └── DaoImpl.java
    │   ├── metier
    │   │   ├── IMetier.java
    │   │   └── MetierImpl.java
    │   └── presentation
    │       ├── PresentationStatique.java
    │       ├── PresentationDynamique.java
    │       ├── PresentationAvecSpring.java
    │       └── PresentationAvecAnnotation.java
    └── resources
        ├── config.txt
        └── config.xml
```

> 📸 **CAPTURE 2 (à placer ici)** : l'arborescence du projet dans IntelliJ (panneau *Project* déplié : `dao`, `metier`, `presentation`, `resources`, `pom.xml`).

### Diagramme de classes

```
Presentation ───────► IMetier ◄- - - MetierImpl ───────► IDao ◄- - - DaoImpl
 (main)               calcul()       dao : IDao          getValue()    getValue()
                                     calcul()
```

- `MetierImpl` dépend de l'**interface** `IDao` et non de la classe concrète `DaoImpl`.
- La présentation dépend de l'interface `IMetier`.

### Code commun aux 4 activités

**`dao/IDao.java`**

```java
package dao;

public interface IDao {
    double getValue();
}
```

**`dao/DaoImpl.java`**

```java
package dao;

import org.springframework.stereotype.Repository;

@Repository
public class DaoImpl implements IDao {
    @Override
    public double getValue() {
        // On simule une récupération de données (par exemple depuis une base de données)
        return 3;
    }
}
```

**`metier/IMetier.java`**

```java
package metier;

public interface IMetier {
    double calcul();
}
```

**`metier/MetierImpl.java`**

```java
package metier;

import dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetierImpl implements IMetier {

    @Autowired
    private IDao dao;

    @Override
    public double calcul() {
        return dao.getValue() * 7;
    }

    public void setDao(IDao dao) {
        this.dao = dao;
    }
}
```

> ℹ️ Les annotations `@Repository`, `@Service` et `@Autowired` ont été ajoutées à l'activité 1-4. Elles sont **ignorées** par les activités 1-1, 1-2 et 1-3 (qui n'activent pas le scan d'annotations), ce qui permet de garder les mêmes classes pour les 4 activités.

### Configuration Maven (`pom.xml`)

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <spring.version>6.1.5</spring.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>${spring.version}</version>
    </dependency>
</dependencies>
```

> `spring-context` embarque transitivement `spring-core` et `spring-beans`, qui sont donc bien disponibles dans le projet.

---

## Activité 1-1 : Injection Statique & Couplage Faible

### Démarche

Création d'un projet Maven **TP1** avec les packages `dao`, `metier` et `presentation`. Dans `PresentationStatique`, on instancie `DaoImpl` et `MetierImpl` avec `new`, puis on injecte le DAO dans le métier via le setter `setDao()`.

### Code source

```java
package presentation;

import dao.DaoImpl;
import metier.MetierImpl;

public class PresentationStatique {
    public static void main(String[] args) {
        // Instanciation directe avec new
        DaoImpl dao = new DaoImpl();
        MetierImpl metier = new MetierImpl();

        // Injection manuelle de la dépendance via le setter
        metier.setDao(dao);

        System.out.println("Résultat du calcul = " + metier.calcul());
    }
}
```

### Exécution

> 📸 **CAPTURE 3 (à placer ici)** : la console IntelliJ après exécution de `PresentationStatique`.

Résultat attendu : `Résultat du calcul = 21.0` (car `3 * 7 = 21`).

### Questions

**9. Expliquez comment cela illustre le couplage faible.**

La classe `MetierImpl` ne possède aucune référence à la classe concrète `DaoImpl` : elle ne connaît que l'interface `IDao`. Elle ne sait donc ni comment la donnée est obtenue, ni d'où elle vient (base de données, fichier, web service...). On peut remplacer `DaoImpl` par n'importe quelle autre implémentation de `IDao` (par exemple `DaoImplBD` ou `DaoImplWS`) **sans modifier une seule ligne** de `MetierImpl`. Le couplage entre la couche métier et la couche d'accès aux données est donc faible : les deux classes communiquent uniquement à travers un contrat (l'interface).

**10. Le principe d'Inversion de Contrôle est-il respecté ? Justifier.**

**Non, il n'est respecté que partiellement.** `MetierImpl` ne crée pas lui-même son DAO : la dépendance lui est fournie de l'extérieur (injection par setter), ce qui est un premier pas vers l'IoC. Mais le **contrôle de la création des objets reste dans notre code** : c'est la classe `PresentationStatique` qui décide quelles classes instancier avec `new` et qui câble elle-même les objets. De plus, `PresentationStatique` dépend des classes concrètes `DaoImpl` et `MetierImpl` : changer d'implémentation oblige à modifier le code source et à recompiler.

**11. Que faudrait-il modifier pour éviter que la classe de présentation contrôle directement la création des objets ?**

Il faut **déléguer la création et le câblage des objets à une entité externe** :

- soit un fichier de configuration qui contient les noms des classes, combiné à la réflexion Java (`Class.forName`) pour instancier dynamiquement les objets (activité 1-2) ;
- soit un conteneur IoC comme **Spring**, qui lit une configuration (XML ou annotations), crée les objets et injecte les dépendances (activités 1-3 et 1-4).

Ainsi la classe de présentation ne fait plus aucun `new` sur les classes concrètes ; elle se contente de demander un objet au conteneur.

---

## Activité 1-2 : Injection Dynamique (Fichier de configuration + Réflexion)

### Démarche

Les noms des classes (`dao.DaoImpl` et `metier.MetierImpl`) sont stockés dans `config.txt`. `PresentationDynamique` les lit, les charge avec `Class.forName()` et injecte le DAO dans le métier par réflexion.

### Code source

```java
package presentation;

import dao.IDao;
import metier.IMetier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class PresentationDynamique {
    public static void main(String[] args) throws Exception {
        // 1. Lire le fichier config.txt depuis le classpath
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        PresentationDynamique.class
                                .getClassLoader()
                                .getResourceAsStream("config.txt")));

        String daoClassName = br.readLine();
        String metierClassName = br.readLine();
        br.close();

        // 2. Charger les classes dynamiquement et les instancier
        Class<?> cDao = Class.forName(daoClassName);
        IDao dao = (IDao) cDao.getDeclaredConstructor().newInstance();

        Class<?> cMetier = Class.forName(metierClassName);
        IMetier metier = (IMetier) cMetier.getDeclaredConstructor().newInstance();

        // 3. Injection par réflexion : appel de setDao(dao)
        Method setDao = cMetier.getMethod("setDao", IDao.class);
        setDao.invoke(metier, dao);

        System.out.println("Résultat dynamique = " + metier.calcul());
    }
}
```

### Exécution

> 📸 **CAPTURE 4 (à placer ici)** : le fichier `config.txt` ouvert dans IntelliJ.
>
> 📸 **CAPTURE 5 (à placer ici)** : la console après exécution de `PresentationDynamique`.

Résultat attendu : `Résultat dynamique = 21.0`.

### Questions

**7. Expliquez comment cela illustre l'injection de dépendances par instanciation dynamique.**

Les noms des classes à utiliser ne sont plus écrits dans le code : ils sont lus au moment de l'exécution depuis `config.txt`. `Class.forName()` charge la classe correspondante en mémoire, `newInstance()` crée l'objet, puis la méthode `setDao` est appelée par réflexion pour injecter le DAO dans le métier. Le programme décide donc **à l'exécution** quelles implémentations utiliser, et non à la compilation.

**8. En quoi cette approche se rapproche-t-elle davantage du principe IoC que l'activité 1-1 ?**

Dans l'activité 1-1, le choix des classes concrètes était **codé en dur** (`new DaoImpl()`). Ici, le code de présentation ne référence plus `DaoImpl` ni `MetierImpl` : le choix des implémentations est déplacé vers un élément **externe** (le fichier de configuration). Pour changer de DAO, il suffit de modifier `config.txt`, sans recompiler. Le contrôle de la création des objets est donc « inversé » : il n'est plus dans le code applicatif mais dans la configuration.

**9. Quels sont les avantages et les limites de cette solution ?**

| Avantages | Limites |
|---|---|
| Aucune modification du code pour changer d'implémentation (seulement `config.txt`) | Code de réflexion lourd et verbeux à écrire |
| Pas de recompilation nécessaire | Erreurs détectées seulement à l'exécution (`ClassNotFoundException`, `NoSuchMethodException`, faute de frappe dans le fichier...) |
| Couplage faible entre présentation et implémentations | Pas de typage fort ni de vérification à la compilation |
| Bon pour comprendre le mécanisme interne de l'IoC | Il faut coder soi-même le câblage de chaque dépendance (tout est manuel dès que l'application grandit) |
| | Format de fichier rudimentaire (ordre des lignes imposé), pas de gestion du cycle de vie ni des singletons |

---

## Activité 1-3 : Spring avec configuration XML

### Démarche

Les beans `DaoImpl` et `MetierImpl` et leur dépendance sont déclarés dans `config.xml` (dépendance Maven `spring-context` ajoutée). `PresentationAvecSpring` charge le contexte Spring et récupère le bean `metier`.

### Fichier `config.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           https://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Bean DAO -->
    <bean id="dao" class="dao.DaoImpl"/>

    <!-- Bean métier, avec injection du DAO via le setter setDao() -->
    <bean id="metier" class="metier.MetierImpl">
        <property name="dao" ref="dao"/>
    </bean>
</beans>
```

### Code source

```java
package presentation;

import metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PresentationAvecSpring {
    public static void main(String[] args) {
        // Chargement du conteneur Spring depuis le fichier XML
        ApplicationContext context = new ClassPathXmlApplicationContext("config.xml");

        // Récupération du bean "metier" déjà créé et câblé par Spring
        IMetier metier = context.getBean("metier", IMetier.class);

        System.out.println("Résultat avec Spring XML = " + metier.calcul());
    }
}
```

### Exécution

> 📸 **CAPTURE 6 (à placer ici)** : le fichier `config.xml` dans IntelliJ.
>
> 📸 **CAPTURE 7 (à placer ici)** : le `pom.xml` avec la dépendance Spring.
>
> 📸 **CAPTURE 8 (à placer ici)** : la console après exécution de `PresentationAvecSpring`.

Résultat attendu : `Résultat avec Spring XML = 21.0`.

### Questions

**6. Comparer cette solution avec l'activité 1-2 en termes de gestion des dépendances.**

| Critère | Activité 1-2 (réflexion + `config.txt`) | Activité 1-3 (Spring + `config.xml`) |
|---|---|---|
| Lecture de la configuration | Écrite à la main (`BufferedReader`) | Gérée par Spring |
| Création des objets | Manuelle avec `Class.forName` + `newInstance` | Automatique par le conteneur |
| Injection | Appel manuel de `setDao` par réflexion | Déclarative : `<property name="dao" ref="dao"/>` |
| Format de configuration | Fichier texte dépendant de l'ordre des lignes | XML structuré avec identifiants de beans |
| Gestion des erreurs | Exceptions de réflexion à gérer | Vérifications et messages d'erreur clairs de Spring |
| Évolutivité | Difficile dès qu'il y a beaucoup de classes | Adaptée aux grosses applications (cycle de vie, singletons, etc.) |

Avec Spring, la gestion des dépendances est **centralisée, déclarative et automatisée**.

**7. Expliquer la différence entre : création manuelle des objets / création par réflexion (`Class.forName`) / création par le conteneur Spring.**

- **Création manuelle (`new`, activité 1-1)** : le développeur écrit explicitement `new DaoImpl()` et câble les objets lui-même. Simple, mais fortement lié au code : changer d'implémentation demande de modifier et recompiler.
- **Création par réflexion (`Class.forName`, activité 1-2)** : les classes sont choisies à l'exécution à partir d'un fichier de configuration. Plus flexible, mais le développeur doit toujours écrire le code de chargement, d'instanciation et d'injection.
- **Création par le conteneur Spring (activité 1-3)** : le conteneur IoC lit la configuration, crée les objets (beans), résout les dépendances et les injecte. Le développeur ne fait que déclarer *quoi* relier à *quoi* ; c'est le framework qui s'occupe du *comment*. C'est la vraie application de l'Inversion de Contrôle.

---

## Activité 1-4 : Spring avec les annotations

### Démarche

Les classes sont annotées avec `@Repository`, `@Service` et `@Autowired`. `PresentationAvecAnnotation` utilise `AnnotationConfigApplicationContext` pour scanner les packages `dao` et `metier` et laisser Spring gérer l'injection.

### Code source

**`DaoImpl`** (extrait)

```java
@Repository
public class DaoImpl implements IDao { ... }
```

**`MetierImpl`** (extrait)

```java
@Service
public class MetierImpl implements IMetier {

    @Autowired
    private IDao dao;

    ...
}
```

**`PresentationAvecAnnotation`**

```java
package presentation;

import metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class PresentationAvecAnnotation {
    public static void main(String[] args) {
        // Scan des packages "dao" et "metier" à la recherche de classes annotées
        ApplicationContext context = new AnnotationConfigApplicationContext("dao", "metier");

        // Récupération du bean de type IMetier
        IMetier metier = context.getBean(IMetier.class);

        System.out.println("Résultat avec Annotations = " + metier.calcul());
    }
}
```

### Exécution

> 📸 **CAPTURE 9 (à placer ici)** : les classes `DaoImpl` et `MetierImpl` avec leurs annotations.
>
> 📸 **CAPTURE 10 (à placer ici)** : la console après exécution de `PresentationAvecAnnotation`.

Résultat attendu : `Résultat avec Annotations = 21.0`.

### Questions

**5. Expliquez comment Spring gère l'injection de dépendances à l'aide des annotations.**

1. `AnnotationConfigApplicationContext("dao", "metier")` demande à Spring de **scanner** ces packages (component scan).
2. Chaque classe annotée `@Repository` ou `@Service` (spécialisations de `@Component`) est détectée et enregistrée comme **bean** dans le conteneur.
3. Spring crée les instances de ces beans.
4. Quand il rencontre `@Autowired` sur l'attribut `dao` de `MetierImpl`, il cherche dans le conteneur un bean compatible avec le type `IDao`, trouve `DaoImpl`, et **l'injecte automatiquement**.
5. Le code de présentation récupère simplement le bean `IMetier` déjà complètement configuré avec `getBean(IMetier.class)`.

**6. Pourquoi cette solution est-elle plus maintenable que la configuration XML ? En quoi cette activité représente-t-elle une application complète du principe IoC ?**

*Plus maintenable que le XML :*
- La configuration est **à côté du code** : plus besoin de maintenir un fichier XML séparé qui peut se désynchroniser du code.
- Moins verbeux : une annotation remplace plusieurs lignes de XML.
- Vérification à la compilation des noms de classes et d'annotations (le XML contient des chaînes de caractères pouvant contenir des fautes de frappe).
- Refactoring facilité (renommer une classe ne casse pas une référence dans un XML).
- Ajouter un nouveau composant ne demande que d'annoter la classe.

*Application complète de l'IoC :*
- Le développeur n'écrit **aucun `new`** pour les composants métier et DAO.
- Le conteneur Spring contrôle **entièrement** : la découverte des classes, leur instanciation, la résolution des dépendances et leur injection.
- Le code applicatif ne fait que *déclarer* ses besoins (`@Autowired`) ; le contrôle du flux de création des objets est totalement inversé au profit du framework.

---

## Synthèse comparative

| Activité | Qui crée les objets ? | Qui injecte ? | Où est la configuration ? | Niveau d'IoC |
|---|---|---|---|---|
| 1-1 Statique | Le développeur (`new`) | Le développeur (`setDao`) | Dans le code | Faible |
| 1-2 Dynamique | Le code via réflexion | Le code via réflexion | `config.txt` | Moyen |
| 1-3 Spring XML | Le conteneur Spring | Le conteneur Spring | `config.xml` | Élevé |
| 1-4 Spring Annotations | Le conteneur Spring | Le conteneur Spring (`@Autowired`) | Annotations dans le code | Complet |

## Conclusion

Ce TP a montré l'évolution de l'injection de dépendances : de l'instanciation manuelle (couplage faible mais contrôle gardé par le développeur) jusqu'à la gestion complète par le conteneur Spring avec annotations. Chaque étape déplace un peu plus la responsabilité de la création et du câblage des objets vers l'extérieur du code applicatif, ce qui rend l'application plus flexible, plus testable et plus maintenable, ce qui constitue la base des architectures microservices.

## Lancer le projet

```bash
# Compiler
mvn clean compile
```

Puis exécuter les classes `main` depuis IntelliJ (clic droit → *Run*) : `PresentationStatique`, `PresentationDynamique`, `PresentationAvecSpring`, `PresentationAvecAnnotation`.
