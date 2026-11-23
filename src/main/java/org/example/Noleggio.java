package org.example;

import java.time.LocalDateTime;

public class Noleggio {
    public final long id;
    public final Utente utente;
    public final Veicolo veicolo;
    public final LocalDateTime inizio;
    public final LocalDateTime fine;
    public final Integer minuti;
    public final Double costo;

    public Noleggio(long id, Utente utente, Veicolo veicolo, LocalDateTime inizio, LocalDateTime fine, Integer minuti, Double costo) {
        this.id = id;
        this.utente = utente;
        this.veicolo = veicolo;
        this.inizio = inizio;
        this.fine = fine;
        this.minuti = minuti;
        this.costo = costo;
    }

    public String toString() {
        return "Noleggio #" + id + " utente=" + utente.id + " veicolo=" + veicolo.codice + " inizio=" + inizio + " fine=" + (fine == null ? "ATTIVO" : fine) + " costo=" + (costo == null ? "-" : costo);
    }
}
