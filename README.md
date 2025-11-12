#  Balade dans un Labyrinthe - Sprint 02 

Ce projet est un jeu d'aventure 2D développé en Java où le joueur incarne un héros explorant un labyrinthe tout en évitant des monstres.

## 🕹️ Déroulement du Jeu

Incarnez un héros et explorez un labyrinthe mystérieux. Le but est de vous déplacer (avec les touches **WASD**  ou bien les touches fléchées du clavier) à travers les chemins (l'herbe) tout en évitant les obstacles infranchissables (l'eau).

Pour attequer le monstre et le tuer , le joueur doit appuyer au moins 3 fois sur la touche espace du clavier à une case prés du monstre : ET LE MONSRE DISPARAIT ! 

Des monstres rôdent également dans le labyrinthe et se déplacent de manière aléatoire. Si vous entrez en collision avec l'un d'eux, votre aventure prend fin et un écran "Game Over" s'affiche. Votre objectif est de naviguer prudemment , d'éviter la rencontre fatale de collecter le maximum de "coins" et d'atteindre le point d'arrivée ! .
 
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
2. Cloner le dépôt Téléchargez le code source depuis GitHub.

Bash

git clone [https://github.com/Radhwen-HAJRI/ACL.git](https://github.com/Radhwen-HAJRI/ACL.git)
3. Accéder au dossier du projet Une fois le clonage terminé, déplacez-vous dans le dossier du projet.

Bash

cd ACL
4. Compiler le projet (Packaging) Utilisez Maven pour créer le fichier .jar exécutable.

Bash

mvn clean package
5. Lancer le jeu ! Exécutez le jeu avec la commande Java suivante :

Bash

java -cp target/labyrinthe-1.0-SNAPSHOT.jar main.main
Informations Techniques
Langage : Java

Version du JDK : OpenJDK 25