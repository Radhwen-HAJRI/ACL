# 🏰 Balade dans un Labyrinthe - Sprint 04

![Java](https://img.shields.io/badge/Language-Java-orange) ![Build](https://img.shields.io/badge/Build-Maven-blue) ![Status](https://img.shields.io/badge/Status-Completed-green)

> Un jeu d'aventure 2D développé en Java où le héros explore un labyrinthe mystérieux tout en évitant des monstres intelligents.

---

## Description

Incarnez un héros intrépide et naviguez à travers **2 niveaux progressifs**. Le but est de trouver votre chemin à travers le labyrinthe, d'éviter les obstacles naturels (eau, murs, arbres, feu) et de survivre aux monstres.

### Objectifs
* **Niveau 1 :** Collectez **10 pièces** (coins jaunes) et trouvez la **Clé** pour déverrouiller l'accès au niveau suivant.
* **Niveau 2 :** Collectez **15 pièces**, survivez au labyrinthe final et atteignez le **Trésor** pour gagner la partie.

---

## 🎮 Gameplay & Captures d'écran

### Interface et Menu
| Menu Principal | HUD (Interface) |
|:---:|:---:|
| <img src="images/menu.png" width="100%"> | <img src="images/hud.png" width="100%"> |
| *Options : New Game, Sound Toggle, Quit* | *Vie (Cœurs) et Score (Pièces)* |

### Exploration des Niveaux
| Niveau 1 (La forêt) | Niveau 2 (Le Donjon) |
|:---:|:---:|
| <img src="images/niveau1.png" width="100%"> | <img src="images/niveau2.png" width="100%"> |
| *Collecte des pièces et recherche de la clé* | *Navigation complexe vers le trésor* |

---

## 🕹️ Contrôles

| Touche | Action |
| :---: | :--- |
| **W / ⬆️** | Avancer (Haut) |
| **A / ⬅️** | Aller à Gauche |
| **S / ⬇️** | Reculer (Bas) |
| **D / ➡️** | Aller à Droite |
| **ESPACE** | Attaquer (si proche d'un monstre) |
| **ECHAP** | Pause / Menu |

---

## Mécaniques de Jeu

* **Combat :** Appuyez sur `ESPACE` près d'un monstre pour l'attaquer (3 coups max par swing).
* **Santé :** Collision avec un monstre = **-1 PV**. Vous avez 3 Cœurs.
    * *0 PV* = Game Over (Overlay Rouge + Retour Menu automatique).
* **IA Monstres :** Comportement aléatoire (errance) ou agressif (chasse le joueur si détecté).
* **Audio :** Gestion des effets sonores (Pas, Coups, Victoire, Défaite) avec option Mute.

---

## Architecture Technique

Le projet respecte une architecture structurée séparant la logique, l'affichage et les données.

### Diagramme de Séquence (Boucle de Jeu)
Ce diagramme illustre le cycle de vie d'une frame de jeu, de l'input utilisateur à l'affichage :

<div align="center">
  <img src="images/Diagramme_de_sequence_UML_Sprint4.png" width="80%" alt="Diagramme de Séquence UML">
</div>

### Structure du Code
* **`main`** : Point d'entrée, gestion de la fenêtre (`GamePanel`) et des entrées (`KeyHandler`).
* **`entity`** : Classes pour le Joueur (`Player`) et les ennemis (`Monster`).
* **`tile`** : Gestion de la carte (`TileManager`) et logique du niveau (`Labyrinthe`).

---

##  Installation et Lancement

Suivez ces étapes pour compiler et jouer au jeu sur votre machine locale.

### Prérequis

* **Git**
* **Java (JDK) :** OpenJDK 25
* **Maven**

### Étapes d'Installation

**1. Créer un dossier de travail**
Ouvrez votre terminal, créez un dossier pour le projet (par exemple, `MesJeux`) et entrez-y.

```bash
mkdir MesJeux
cd MesJeux
```
## 2. Cloner le dépôt Téléchargez le code source depuis GitHub.

```Bash
git clone https://github.com/Radhwen-HAJRI/ACL.git
```
## 3. Accéder au dossier du projet Une fois le clonage terminé, déplacez-vous dans le dossier du projet.

```Bash
cd ACL
```

## 4. Compiler le projet (Packaging) Utilisez Maven pour créer le fichier .jar exécutable.

```Bash

mvn clean package
```

## 5. Lancer le jeu ! Exécutez le jeu avec la commande suivante :

```Bash
java -cp target/labyrinthe-1.0-SNAPSHOT.jar main.main
```

## Informations Techniques
Langage : Java

Version du JDK : OpenJDK 25

Outils : Maven (build), Swing (rendu), Java Sound API (sons).
Structure :

* Packages :main (GamePanel, KeyHandler, SoundManager), entity (Player, Monster), tile (TileManager, Labyrinthe).
* Ressources :/resources/tiles/ (images), /sounds/ (WAV), /maps/ (map01.txt, map02.txt).
* Fonctionnalités clés : Caméra follow, collisions solides, IA monstres (chase/attack), HUD dynamique, menu toggle son, reset multi-niveaux.