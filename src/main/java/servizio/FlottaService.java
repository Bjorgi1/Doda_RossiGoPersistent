package servizio;

import database.DatabaseManager;
import org.example.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class FlottaService {
    private final DatabaseManager db;
    private static final Pattern CF = Pattern.compile("(?i)^[A-Z]{6}[0-9]{2}[A-EHLMPRST][0-9]{2}[A-Z][0-9]{3}[A-Z]$");

    public FlottaService() {
        this(new DatabaseManager());
    }

    public FlottaService(DatabaseManager db) {
        this.db = db;
    }

    public void registraVeicolo(Veicolo v) {
        try (Connection c = db.connessione(); PreparedStatement p = c.prepareStatement("INSERT INTO veicoli(codice,tipo,batteria,stato) VALUES(?,?,?,?)")) {
            p.setInt(1, v.codice);
            p.setString(2, v.getTipo());
            p.setInt(3, v.batteria);
            p.setString(4, v.stato);
            p.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Impossibile registrare il veicolo (codice gia presente?)", e);
        }
    }

    public void registraUtente(Utente u) {
        if (u.codiceF == null || !CF.matcher(u.codiceF).matches())
            throw new CodiceFiscaleNonValidoException("Codice fiscale non valido: " + u.codiceF);
        try (Connection c = db.connessione(); PreparedStatement p = c.prepareStatement("INSERT INTO utenti(id,nome,codice_fiscale,credito,email) VALUES(?,?,?,?,?)")) {
            p.setInt(1, u.id);
            p.setString(2, u.nome);
            p.setString(3, u.codiceF.toUpperCase(Locale.ROOT));
            p.setDouble(4, u.credito);
            p.setString(5, u.getEmail());
            p.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Impossibile registrare utente (ID o CF gia presente?)", e);
        }
    }

    private Utente leggiUtente(ResultSet r) throws SQLException {
        return new Utente(r.getInt("id"), r.getString("nome"), r.getString("codice_fiscale"), r.getDouble("credito"), r.getString("email").toCharArray());
    }

    private Veicolo leggiVeicolo(ResultSet r) throws SQLException {
        int id = r.getInt("codice"), b = r.getInt("batteria");
        String s = r.getString("stato");
        return "Bici".equals(r.getString("tipo")) ? new Bici(id, b, s) : new Monopattino(id, b, s);
    }

    private Utente cercaUtente(Connection c, int id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM utenti WHERE id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new UtenteNonTrovato("Utente " + id + " non trovato");
                return leggiUtente(r);
            }
        }
    }

    private Veicolo cercaVeicolo(Connection c, int codice) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM veicoli WHERE codice=?")) {
            p.setInt(1, codice);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new VeicoloNonDisponibileException("Veicolo " + codice + " non trovato");
                return leggiVeicolo(r);
            }
        }
    }

    public Utente cercaId(int id) {
        try (Connection c = db.connessione()) {
            return cercaUtente(c, id);
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore ricerca utente", e);
        }
    }

    public Veicolo cercaVeicoloPerCodice(int codice) {
        try (Connection c = db.connessione()) {
            return cercaVeicolo(c, codice);
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore ricerca veicolo", e);
        }
    }

    public List<Veicolo> elencoVeicoli() {
        List<Veicolo> out = new ArrayList<>();
        try (Connection c = db.connessione(); PreparedStatement p = c.prepareStatement("SELECT * FROM veicoli ORDER BY codice"); ResultSet r = p.executeQuery()) {
            while (r.next()) out.add(leggiVeicolo(r));
            return out;
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore elenco veicoli", e);
        }
    }

    public List<Utente> elencoUtenti() {
        List<Utente> out = new ArrayList<>();
        try (Connection c = db.connessione(); PreparedStatement p = c.prepareStatement("SELECT * FROM utenti ORDER BY id"); ResultSet r = p.executeQuery()) {
            while (r.next()) out.add(leggiUtente(r));
            return out;
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore elenco utenti", e);
        }
    }

    public List<Noleggio> elencoNoleggi() {
        List<Noleggio> out = new ArrayList<>();
        try (Connection c = db.connessione(); PreparedStatement p = c.prepareStatement("SELECT * FROM noleggi ORDER BY id"); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Utente u = cercaUtente(c, r.getInt("utente_id"));
                Veicolo v = cercaVeicolo(c, r.getInt("veicolo_codice"));
                String fine = r.getString("fine");
                double cost = r.getDouble("costo");
                out.add(new Noleggio(r.getLong("id"), u, v, LocalDateTime.parse(r.getString("inizio")), fine == null ? null : LocalDateTime.parse(fine), r.getObject("minuti") == null ? null : r.getInt("minuti"), r.getObject("costo") == null ? null : cost));
            }
            return out;
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore elenco noleggi", e);
        }
    }

    public String SbloccaVeicolo(int codice, int minutiPrevisti, Utente u, int id) {
        if (u == null || u.id != id) throw new UtenteNonTrovato("ID utente non corrispondente");
        if (minutiPrevisti <= 0) throw new IllegalArgumentException("Minuti previsti devono essere positivi");
        try (Connection c = db.connessione()) {
            c.setAutoCommit(false);
            try {
                Utente registrato = cercaUtente(c, id);
                Veicolo v = cercaVeicolo(c, codice);
                if (!"disponibile".equals(v.stato))
                    throw new VeicoloNonDisponibileException("Veicolo non disponibile: " + v.stato);
                try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM noleggi WHERE utente_id=? AND fine IS NULL")) {
                    p.setInt(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) throw new VeicoloNonDisponibileException("Utente con noleggio gia attivo");
                    }
                }
                if (v.batteria < 15 || v.consumoBatteriaPerMinuto(minutiPrevisti) > v.batteria)
                    throw new BatteriaScaricaException("Batteria insufficiente per il noleggio");
                double stimato = 1 + v.calcolaCosto(minutiPrevisti);
                if (registrato.credito < stimato)
                    throw new CreditoInsufficienteException("Credito insufficiente: servono EUR " + stimato);
                try (PreparedStatement p = c.prepareStatement("INSERT INTO noleggi(utente_id,veicolo_codice,inizio) VALUES(?,?,?)")) {
                    p.setInt(1, id);
                    p.setInt(2, codice);
                    p.setString(3, LocalDateTime.now().toString());
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE veicoli SET stato='occupato' WHERE codice=?")) {
                    p.setInt(1, codice);
                    p.executeUpdate();
                }
                c.commit();
                return "Veicolo " + codice + " sbloccato. Costo stimato EUR " + String.format(Locale.ROOT, "%.2f", stimato);
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore sblocco veicolo", e);
        }
    }

    public void terminaNoleggio(int codiceV, int minutiUsati) {
        if (minutiUsati <= 0) throw new IllegalArgumentException("Minuti devono essere positivi");
        try (Connection c = db.connessione()) {
            c.setAutoCommit(false);
            try {
                Veicolo v = cercaVeicolo(c, codiceV);
                int utenteId;
                long noleggioId;
                try (PreparedStatement p = c.prepareStatement("SELECT id,utente_id FROM noleggi WHERE veicolo_codice=? AND fine IS NULL")) {
                    p.setInt(1, codiceV);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next())
                            throw new VeicoloNonDisponibileException("Nessun noleggio attivo per il veicolo " + codiceV);
                        noleggioId = r.getLong(1);
                        utenteId = r.getInt(2);
                    }
                }
                Utente u = cercaUtente(c, utenteId);
                double costo = 1 + v.calcolaCosto(minutiUsati);
                if (u.credito < costo)
                    throw new CreditoInsufficienteException("Credito insufficiente per chiudere: servono EUR " + costo + "; ricaricare prima il conto");
                int nuovaBatteria = Math.max(0, v.batteria - v.consumoBatteriaPerMinuto(minutiUsati));
                try (PreparedStatement p = c.prepareStatement("UPDATE utenti SET credito=ROUND(credito-?,2) WHERE id=?")) {
                    p.setDouble(1, costo);
                    p.setInt(2, utenteId);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE veicoli SET stato=?,batteria=? WHERE codice=?")) {
                    p.setString(1, nuovaBatteria < 15 ? "ricarica" : "disponibile");
                    p.setInt(2, nuovaBatteria);
                    p.setInt(3, codiceV);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE noleggi SET fine=?,minuti=?,costo=? WHERE id=?")) {
                    p.setString(1, LocalDateTime.now().toString());
                    p.setInt(2, minutiUsati);
                    p.setDouble(3, costo);
                    p.setLong(4, noleggioId);
                    p.executeUpdate();
                }
                c.commit();
                System.out.printf(Locale.ROOT, "Noleggio terminato. Totale EUR %.2f, batteria %d%%%n", costo, nuovaBatteria);
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore chiusura noleggio", e);
        }
    }

    public void mettiInRicarica(String codice) {
        int id;
        try {
            id = Integer.parseInt(codice);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Codice non numerico");
        }
        aggiornaVeicolo(id, "ricarica", null);
    }

    public void completaRicarica(int codice) {
        aggiornaVeicolo(codice, "disponibile", 100);
    }

    private void aggiornaVeicolo(int codice, String stato, Integer batteria) {
        try (Connection c = db.connessione()) {
            Veicolo v = cercaVeicolo(c, codice);
            if ("occupato".equals(v.stato)) throw new VeicoloNonDisponibileException("Veicolo occupato");
            try (PreparedStatement p = c.prepareStatement("UPDATE veicoli SET stato=?,batteria=? WHERE codice=?")) {
                p.setString(1, stato);
                p.setInt(2, batteria == null ? v.batteria : batteria);
                p.setInt(3, codice);
                p.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore ricarica", e);
        }
    }

    public void ricaricaCredito(int id, double importo) {
        if (!Double.isFinite(importo) || importo <= 0) throw new IllegalArgumentException("Importo non valido");
        try (Connection c = db.connessione()) {
            cercaUtente(c, id);
            try (PreparedStatement p = c.prepareStatement("UPDATE utenti SET credito=ROUND(credito+?,2) WHERE id=?")) {
                p.setDouble(1, importo);
                p.setInt(2, id);
                p.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore ricarica credito", e);
        }
    }

    public void eliminaVeicolo(int codice) {
        try (Connection c = db.connessione(); PreparedStatement p = c.prepareStatement("DELETE FROM veicoli WHERE codice=?")) {
            p.setInt(1, codice);
            if (p.executeUpdate() == 0) throw new VeicoloNonDisponibileException("Veicolo non trovato");
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Impossibile eliminare: veicolo usato nei noleggi", e);
        }
    }
}
