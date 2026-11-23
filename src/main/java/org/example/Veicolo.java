package org.example;

public abstract class Veicolo {
    public final int codice;
    public int batteria;
    public String stato;

    public Veicolo(int codice, int batteria, String stato) {
        if (codice <= 0 || batteria < 0 || batteria > 100 || !java.util.Set.of("disponibile", "occupato", "ricarica").contains(stato))
            throw new IllegalArgumentException("Dati veicolo non validi");
        this.codice = codice;
        this.batteria = batteria;
        this.stato = stato;
    }

    public abstract double calcolaCosto(int minuti);

    public abstract int consumoBatteriaPerMinuto(int minuti);

    public abstract int VelocitaMassima();

    public int getCodice() {
        return codice;
    }

    public int getLivelloBatteria() {
        return batteria;
    }

    public String getStato() {
        return stato;
    }

    public String getTipo() {
        return getClass().getSimpleName();
    }

    public String toString() {
        return getTipo() + " #" + codice + " | batteria " + batteria + "% | " + stato;
    }
}
