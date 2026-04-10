# Projet de Synthèse L3 — Langage de script pour animations graphiques simples

## Auteurs

- Senad SENAD

## Présentation générale

Ce projet consiste à développer progressivement un petit langage de script fondé sur des S-expressions pour piloter des éléments graphiques simples dans un espace 2D.  
L’objectif est de pouvoir manipuler dynamiquement des objets graphiques (rectangle, ovale, image, texte), leur associer des commandes, les organiser dans des conteneurs, puis enrichir le système avec des scripts utilisateurs, des expressions, des conditionnelles et des boucles.

Le projet a été réalisé en Java sous Eclipse à partir des briques fournies dans les sujets, en particulier la couche graphique `graphicLayer` et le parseur de S-expressions.

---

## Contenu rendu

### Partie 1

#### Exercice 1
Animation d’un rectangle `robi` dans un `GSpace` :
- déplacement sur les bords internes de la fenêtre ;
- adaptation au redimensionnement ;
- changement périodique de couleur.

#### Exercice 2
Première version de l’interpréteur de script :
- interprétation d’un script composé de plusieurs S-expressions ;
- gestion de commandes simples comme `setColor`, `translate`, `sleep`.

#### Exercice 3
Introduction du modèle par commandes :
- création d’une interface `Command` ;
- séparation de la logique des commandes dans des classes dédiées.

#### Exercice 4
Mise en place d’un environnement de références :
- classe `Environment` pour stocker les références nommées ;
- classe `Reference` pour associer un objet Java à ses commandes ;
- exécution des commandes sans chaîne de conditions centrale ;
- ajout dynamique d’éléments avec `add` ;
- suppression dynamique avec `del`.

#### Exercice 5
Ajout d’éléments dans des conteneurs :
- gestion des noms qualifiés du type `space.robi.im` ;
- support des conteneurs imbriqués ;
- suppression récursive d’un sous-arbre de références.

#### Exercice 6
Scripts utilisateurs :
- ajout de scripts sur une référence avec `addScript` ;
- enregistrement de paramètres ;
- exécution d’un script avec liaison de `self` et des arguments.

#### Exercice 7
Expressions et structures de contrôle :
- opérateurs arithmétiques `+ - * /` ;
- comparaisons `< > =` ;
- affectation avec `set` ;
- conditionnelle `if` ;
- boucle `while` ;
- bloc séquentiel `begin`.

---

## Structure principale du projet

### Package `api`
Le package `api` contient le cœur de l’interpréteur.

- `Environment` : table des références nommées.
- `Reference` : associe un receiver et ses commandes primitives.
- `ValueReference` : encapsule une valeur calculée.
- `Interpreter` : évalue les S-expressions.
- `Command` : interface commune aux commandes.
- `AddElement`, `DelElement` : ajout et suppression dynamique.
- `AddScript`, `ScriptCommand`, `StoredScript`, `ScriptTools` : infrastructure des scripts utilisateurs.
- `GraphicReferenceFactory` : fabrique de références configurées pour les objets graphiques.
- `NewElement`, `NewString`, `NewImage` : création dynamique d’objets.
- `SetColor`, `SetDim`, `Translate`, `ImageTranslate`, `Sleep` : commandes primitives.

### Packages `exerciceX`
Chaque exercice est isolé dans son propre package afin de conserver une progression claire et démontrable.

### Package `tests`
Le package `tests` contient les tests unitaires ciblant surtout la couche API non graphique :
- environnement ;
- références ;
- interpréteur ;
- scripts ;
- logique des noms qualifiés ;
- création dynamique d’éléments.

---

## Choix techniques

### 1. Séparation entre interprétation et exécution
L’interpréteur ne contient pas une grande double conditionnelle `if/else` sur tous les objets et toutes les commandes.  
Le dispatch se fait via :
- `Environment` pour retrouver la référence cible ;
- `Reference` pour retrouver la commande à exécuter.

Cela rend l’architecture plus extensible.

### 2. Références qualifiées
Les objets ajoutés dynamiquement sont enregistrés avec des noms qualifiés :
- `space.robi`
- `space.robi.im`
- `space.robi.label`

Ce mécanisme permet d’avoir plusieurs objets portant le même nom local dans des conteneurs différents tout en évitant les collisions globales.

### 3. Scripts utilisateurs
Les scripts sont stockés comme une signature + une liste d’expressions.  
Lors de l’exécution :
- `self` est lié au nom qualifié du receveur ;
- les autres paramètres sont remplacés par les arguments effectifs ;
- les expressions sont réinjectées dans l’interpréteur standard.

### 4. Tests unitaires
Les tests portent surtout sur la couche non graphique, car elle concentre la logique métier :
- calculs ;
- comparaisons ;
- affectations ;
- boucles ;
- scripts ;
- enregistrement et suppression de références.

---

## Exemples d’utilisation

### Ajout dynamique d’un rectangle
```lisp
(space add robi (Rect new))
(space.robi setColor yellow)
(space.robi translate 50 30)
```

### Ajout d’un élément dans un conteneur
```lisp
(space add robi (Rect new))
(space.robi add eye (Oval new))
(space.robi.eye setColor red)
```

### Script utilisateur
```lisp
(space addScript makeRect (
  (self name size color)
  (self add name (Rect new))
  (self.name setDim size size)
  (self.name setColor color)
))
```

### Expressions et boucle
```lisp
(begin
  (set i 0)
  (while (< i 5)
    (set i (+ i 1))
  )
  i
)
```

### Partie 2 — Application distribuée + IHM

Architecture client–serveur :
- Serveur = interpréteur (réutilisation complète de la partie 1)
- Client = interface graphique + envoi de commandes
- Communication via objets sérialisés (requêtes / réponses)

 Fonctionnalités implémentées :
- Envoi de commandes depuis le client
- Exécution côté serveur
- Retour de l’état graphique (SceneData)
- Synchronisation client ↔ serveur

Interface graphique (Swing) :
- Visualisation de la scène
- Interaction via boutons (add, move, etc.)
- Console de commandes
- Arbre des éléments

 Persistance :
- Sauvegarde / chargement de scènes (sérialisation)

---


Partie 3 – Machine à états (Robibot)
------------------------------------

### Principe

Un second serveur (BotServer) a été mis en place afin d’implémenter une **machine à états pilotant des éléments graphiques**.

Contrairement à l’architecture client/serveur précédente :

*   le serveur gère entièrement la scène
    
*   des agents (bots) modifient automatiquement cette scène
    
*   les clients ne font que lire et afficher
    

Le client (BotClient) est donc **passif** :

*   il envoie périodiquement GET\_SCENE
    
*   il redessine la scène reçue
    
*   il n’exécute aucune logique
    

Cela garantit un **arbitrage centralisé côté serveur** et une synchronisation entre clients.

### Modèle de machine à états

Chaque bot contrôle un élément graphique (ex : space.robi1) et suit une **machine à états finie**.

#### États

Le bot possède 4 états correspondant à des déplacements diagonaux :

*   UP\_LEFT
    
*   UP\_RIGHT
    
*   DOWN\_LEFT
    
*   DOWN\_RIGHT
    

Chaque état encode une direction (dx, dy).

#### Transitions

À chaque tick :

*   la prochaine position est calculée
    
*   si une bordure est atteinte :
    
    *   collision horizontale → inversion de dx
        
    *   collision verticale → inversion de dy
        
*   un changement d’état est déclenché
    
*   la couleur de l’élément est modifiée aléatoirement
    

Le comportement obtenu est un déplacement avec rebond sur les bords.

Difficultés rencontrées
-----------------------

Plusieurs difficultés ont été rencontrées tout au long du projet :

*   **Conception de l’architecture**
    
    *   passage d’une API locale (partie 1) à une architecture client/serveur (partie 2)
        
    *   nécessité de ne pas dupliquer la logique métier côté serveur
        
*   **Réutilisation de l’API**
    
    *   intégrer correctement les commandes existantes dans le serveur
        
    *   éviter de réécrire des comportements déjà implémentés
        
*   **Gestion des références et de l’environnement**
    
    *   manipulation des chemins (space.robi.x)
        
    *   suppression récursive et cohérence des références
        
*   **Gestion des scripts**
    
    *   interprétation correcte des S-expressions
        
    *   persistance des scripts dans l’environnement serveur
        
    *   exécution dynamique sans casser l’état existant
        
*   **Communication client/serveur**
    
    *   sérialisation des données (SceneData, ElementData)
        
    *   gestion des requêtes et réponses
        
    *   maintien d’un protocole simple mais fiable
        
*   **Synchronisation**
    
    *   garantir que plusieurs clients voient le même état
        
    *   centraliser toute la logique côté serveur
        
*   **Gestion des limites (out of bounds)**
    
    *   empêcher les éléments de sortir de leur conteneur
        
    *   gérer les cas sans générer d’erreurs
        
*   **Interface graphique**
    
    *   gestion des interactions utilisateur
        
    *   affichage cohérent de la scène et de l’arbre
        
*   **Machine à états**
    
    *   choix d’un modèle pertinent pour le mouvement
        
    *   gestion des transitions et du comportement dynamique
        
*   **Contraintes de temps**
    
    *   priorisation des fonctionnalités essentielles
        
    *   certains choix ont été faits pour avancer rapidement plutôt que pour être optimaux
        

Améliorations et optimisations possibles
----------------------------------------

Plusieurs axes d’amélioration sont possibles à l’échelle du projet :

*   **Performances**
    
    *   éviter la reconstruction complète de la scène à chaque requête
        
    *   mettre en place des mises à jour incrémentales
        
*   **Architecture**
    
    *   mieux séparer les responsabilités (API / serveur / simulation)
        
    *   réduire certaines dépendances fortes
        
*   **Scalabilité**
    
    *   améliorer la gestion de plusieurs clients simultanés
        
    *   optimiser la gestion des données pour de grandes scènes
        
*   **Réseau**
    
    *   réduire la quantité de données envoyées (diff au lieu de snapshot complet)
        
    *   améliorer la gestion des connexions
        
*   **Gestion des scripts**
    
    *   ajouter des validations plus robustes
        
    *   améliorer les messages d’erreur
        
*   **Machine à états**
    
    *   enrichir les comportements (interactions entre bots, états supplémentaires)
        
    *   rendre les transitions configurables
        
    *   Collision
        
*   **Interface utilisateur**
    
    *   améliorer l’ergonomie
        
    *   ajouter plus de contrôles visuels
        
    *   Ajouter gestion des images API existant
        
*   **Robustesse**
    
    *   meilleure gestion des erreurs
        
    *   ajout de tests supplémentaires (notamment côté serveur)
        
*   **Maintenabilité**
    
    *   factorisation de certaines parties du code
        
    *   amélioration de la lisibilité globale