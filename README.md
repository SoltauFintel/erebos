# Erebos Eclipse Workspace Finder

Mit diesem Kommandozeilentool kann man Eclipse Workspaces finden und die darin enthaltenen **Projekte** analysieren.
Wo liegt das Projektverzeichnis? Wird ein **Git Repository** verwendet? Wie ist die URL vom **Remote-Git-Repository**?
Das Programm erstellt im aktuellen Verzeichnis die Datei **workspaces.html**. Diese Datei listet alle gefundenen Workspaces
und Projekte. Außerdem werden alle Git Repos und mehrfach vorhandene Projekte aufgeführt.
Erebos (=griechischer Gott der Finsternis) wurde vorrangig zum Finden von Eclipse-Java-Projekten auf einem Windows-PC entwickelt.

## Build und Ausführung

Ant-Datei build.xml (Taget build) ausführen. Die ausführbare Anwendung liegt dann im Ordner erebos/build/install/erebos
und kann mit bin/erebos.bat _{pfad}_ auf einem Windows-PC gestartet werden.
_{pfad}_ ist dabei ein Workspace-Ordner oder ein Ordner, in dem nach Workspaces gesucht werden soll.

Tooling: Java 8, Gradle 1.9, Ant, Eclipse Mars.1

## Lizenz

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)
