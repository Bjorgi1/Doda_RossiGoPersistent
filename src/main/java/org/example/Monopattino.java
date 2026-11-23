package org.example;

public class Monopattino extends Veicolo {
    public int velocita = 20;
    public double tariffaM = 0.25;

    public Monopattino(int codice, int batteria, String stato) {
        super(codice, batteria, stato);
    }

    @Override
    public double calcolaCosto(int minuti) {
        if (minuti < 0) throw new IllegalArgumentException("Minuti negativi");
        return Math.round(minuti * tariffaM * 100.0) / 100.0;
    }

    @Override
    public int consumoBatteriaPerMinuto(int minuti) {
        if (minuti < 0) throw new IllegalArgumentException("Minuti negativi");
        return minuti;
    }

    @Override
    public int VelocitaMassima() {
        return velocita;
    }
}
