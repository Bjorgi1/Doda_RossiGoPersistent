package database;

import org.example.ErroreDatabaseException;

import java.sql.*;
import java.nio.file.*;

public class DatabaseManager {
    private final String url;

    public DatabaseManager() {
        this(Path.of("database", "rossigo.db"));
    }

    public DatabaseManager(Path file) {
        try {
            Path absolute = file.toAbsolutePath();
            Files.createDirectories(absolute.getParent());
            url = "jdbc:sqlite:" + absolute;
        } catch (Exception e) {
            throw new ErroreDatabaseException("Impossibile preparare database", e);
        }
        inizializza();
    }

    public Connection connessione() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("PRAGMA busy_timeout=5000");
        }
        return c;
    }

    private void inizializza() {
        String[] ddl = {
                "CREATE TABLE IF NOT EXISTS veicoli (codice INTEGER PRIMARY KEY,tipo TEXT NOT NULL CHECK(tipo IN ('Bici','Monopattino')),batteria INTEGER NOT NULL CHECK(batteria BETWEEN 0 AND 100),stato TEXT NOT NULL CHECK(stato IN ('disponibile','occupato','ricarica')))",
                "CREATE TABLE IF NOT EXISTS utenti (id INTEGER PRIMARY KEY,nome TEXT NOT NULL,codice_fiscale TEXT NOT NULL UNIQUE,credito REAL NOT NULL CHECK(credito>=0),email TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS noleggi (id INTEGER PRIMARY KEY AUTOINCREMENT,utente_id INTEGER NOT NULL REFERENCES utenti(id),veicolo_codice INTEGER NOT NULL REFERENCES veicoli(codice),inizio TEXT NOT NULL,fine TEXT,minuti INTEGER,costo REAL)",
                "CREATE UNIQUE INDEX IF NOT EXISTS un_noleggio_attivo_per_veicolo ON noleggi(veicolo_codice) WHERE fine IS NULL"
        };
        try (Connection c = connessione(); Statement s = c.createStatement()) {
            for (String q : ddl) s.execute(q);
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore creazione tabelle", e);
        }
    }
}
