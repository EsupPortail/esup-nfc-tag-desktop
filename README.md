ESUP-NFC-TAG-DESKTOP
====================

Esup-nfc-tag-desktop permet d'encoder et de lire les cartes Mifare Desfire EV1. 
Le client s'appuie sur la platefome https://github.com/EsupPortail/esup-nfc-tag-server qui calcule les commandes (APDU) à transmettre à la carte.

Esup-nfc-tag-desktop permet d'utiliser un lecteur RFID USB pour badger, en utilisant l'UID (CSN) ou en faisant une lecture d'un fichier Desfire (avec autentification AES)

L'application est packagée sous la forme d'un jar comprenant les dépendences : esupnfctagdesktop-1.0-SNAPSHOT-jar-with-dependencies.jar

## Fonctionalités

1 - L'application esup-nfc-tag-desktop se comporte de la même manière que l'application Android https://github.com/EsupPortail/esup-nfc-tag-droid

2 - L'application repose sur un composant webview JavaFX qui se connecte et affiche la vue fournie par esup-nfc-tag-server

3 - Après l'authentification Shibboleth il faut choisir la salle de badgeage

4 - Pour badger il suffit de poser une carte sur le lecteur nfc

## Environnement

### Pré-requis

- Java 1.8, JavaFX
- Maven

### Logiciel

L'application est prévue pour tourner avec java 8 et JavaFX.

### Materiel

- un lecteur de carte USB compatible PC/SC (ex: Indentive Cloud 4700f, OMNIKEY CardMan 5x21-CL...)

## Compilation esup-nfc-client

 * modifier src/main/resources/esupnfctag.properties pour changer l'adresse url du serveur esup-nfc-tag-server

 * dans le dossier esup-nfc-tag-desktop executer
 ```
 mvn clean package
 ```

## Integration dans esup-nfc-tag-server

 * copier le JAR dans EsupNfcTagServer pour le mettre à disposition des utilisateurs :
```
cp target/esupnfctagdesktop-1.0-SNAPSHOT-jar-with-dependencies.jar /<path to>/esup-nfc-tag-server/src/main/resources/jar/esupnfctagdesktop.jar
```
 * recompiler et redéployer esup-nfc-tag-server. Au redémarrage d'esup-nfc-tag-server la nouvelle version du jar sera prise en compte
