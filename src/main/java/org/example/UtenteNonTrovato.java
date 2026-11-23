package org.example;

public class UtenteNonTrovato extends RossiGoException {
    public UtenteNonTrovato(String messaggio) {
        super(messaggio);
    }

    public UtenteNonTrovato(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
