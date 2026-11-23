# RossiGo Persistente — Java 21, Maven e SQLite

Progetto basato su RossiGo sviluppato in classe. Tutte le classi originali sono nel package `org.example`; il servizio e il database sono separati nei package `servizio` e `database`. Non sono presenti copie delle classi in altri package.

## Apertura in IntelliJ IDEA
1. Estrai lo ZIP.
2. Apri **la cartella che contiene pom.xml** con IntelliJ IDEA.
3. Seleziona **JDK 21** e attendi il download delle dipendenze Maven (serve Internet al primo caricamento).
4. Avvia `org.example.Main`.

Il database `database/rossigo.db` viene creato automaticamente alla prima esecuzione. Non serve installare SQLite separatamente.

## Funzioni
Registrazione di bici e monopattini, registrazione utenti, elenchi, sblocco, chiusura noleggio, aggiornamento del credito, ricarica dei mezzi, storico e rimozione dei mezzi senza noleggi associati. I dati rimangono salvati dopo la chiusura.

## Regole dimostrative
Tariffa di sblocco: 1 euro; bici 0,15 euro/min, monopattino 0,25 euro/min; consumo bici 0,5%/min arrotondato per eccesso, monopattino 1%/min; batteria minima 15%. Sono parametri dimostrativi, modificabili secondo le istruzioni del docente. Il codice fiscale deve rispettare il formato italiano di 16 caratteri.

## Struttura
- `src/main/java/org/example`: modelli, eccezioni e Main (come il progetto di classe)
- `src/main/java/servizio/FlottaService.java`: operazioni di noleggio
- `src/main/java/database/DatabaseManager.java`: connessione e creazione tabelle SQLite
- `database/schema.sql`: schema di riferimento
- `pom.xml`: dipendenza JDBC SQLite

## Nota
La chiusura del noleggio richiede credito sufficiente. Se manca, ricaricare il credito e riprovare. Un mezzo con storico di noleggi non puo essere eliminato per conservare la cronologia.
