package org.example;

public class RossiGoException extends RuntimeException {
    public RossiGoException(String messaggio) {
        super(messaggio);
    }

    public RossiGoException(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
