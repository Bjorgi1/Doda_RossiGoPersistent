package org.example;

public class Utente {
    public int id;
    public String nome;
    public String codiceF;
    public char[] email;
    public double credito;

    public Utente(int id, String nome, String codiceF, double credito, char[] email) {
        if (id <= 0 || nome == null || nome.isBlank() || (!Double.isFinite(credito) || credito < 0) || email == null || email.length == 0)
            throw new IllegalArgumentException("Dati utente non validi");
        this.id = id;
        this.nome = nome;
        this.codiceF = codiceF;
        this.credito = credito;
        this.email = email.clone();
    }

    public int getId() {
        return id;
    }

    public String getCodiceFiscale() {
        return codiceF;
    }

    public double getCredito() {
        return credito;
    }

    public String getEmail() {
        return new String(email);
    }

    public String toString() {
        return id + " - " + nome + " | credito EUR " + String.format(java.util.Locale.ROOT, "%.2f", credito);
    }
}
