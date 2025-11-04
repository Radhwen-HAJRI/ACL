# Balade dans un Labyrinthe 

### HOW TO RUN

## STEPS

clone the repository

```bash 
git@github.com:Radhwen-HAJRI/ACL.git

```

## STEP 01 - DOWNLOAD MAVEN AND JAVA

OpenJDK 25


## STEP 02 - RUN THE FOLLOWING COMMAND
```bash 

mvn clean package

```


```bash 

java -cp target/labyrinthe-1.0-SNAPSHOT.jar main.main

```

##  Description
Jeu en Java où le joueur incarne un héros explorant un labyrinthe fixe à la recherche d’un trésor tout en évitant les monstres mobiles.

Cette version (v1.0) inclut :

* Un labyrinthe fixe (grille 16x16 avec tiles herbe pour chemins et eau pour obstacles).
* Héros customisé avec sprites animés (mouvements WASD, animations directionnelles).
* Respect des obstacles : Le héros ne peut pas traverser les murs (eau).
* Monstres mobiles qui se déplacent aléatoirement sur les chemins valides.
* Game Over : Collision avec un monstre arrête le jeu et affiche un écran de fin.

##  Compilation et exécution
**Langage :** Java  
**Version du JDK :** OpenJDK 25

