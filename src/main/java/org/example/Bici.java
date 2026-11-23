package org.example;

public class Bici extends Veicolo {
    public int marce;
    public double tariffa;

    public Bici(int marce, double tariffa, int codice, int batteria, String stato) {
        super(codice, batteria, stato);
        if (marce <= 0 || (!Double.isFinite(tariffa) || tariffa < 0))
            throw new IllegalArgumentException("Marce o tariffa non valide");
        this.marce = marce;
        this.tariffa = tariffa;
    }

    public Bici(int codice, int batteria, String stato) {
        this(7, 0.15, codice, batteria, stato);
    }

    @Override
    public double calcolaCosto(int minuti) {
        if (minuti < 0) throw new IllegalArgumentException("Minuti negativi");
        return Math.round(minuti * tariffa * 100.0) / 100.0;
    }

    @Override
    public int consumoBatteriaPerMinuto(int minuti) {
        if (minuti < 0) throw new IllegalArgumentException("Minuti negativi");
        return (int) Math.ceil(minuti * 0.5);
    }

    @Override
    public int VelocitaMassima() {
        return 25;
    }
}
