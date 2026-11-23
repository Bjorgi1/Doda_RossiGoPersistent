package org.example;

public class BatteriaScaricaException extends RossiGoException {
    public BatteriaScaricaException(String messaggio) {
        super(messaggio);
    }

    public BatteriaScaricaException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
