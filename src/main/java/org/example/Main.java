package org.example;

import servizio.FlottaService;

import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);

    private static String leggi(String testo) {
        System.out.print(testo);
        return in.nextLine().trim();
    }

    private static int intero(String testo) {
        return Integer.parseInt(leggi(testo));
    }

    private static double decimale(String testo) {
        return Double.parseDouble(leggi(testo).replace(',', '.'));
    }

    public static void main(String[] args) {
        FlottaService flotta = new FlottaService();
        System.out.println("=== ROSSIGO PERSISTENTE - SQLite ===");
        while (true) {
            System.out.println("\n1) Registra veicolo  2) Registra utente  3) Elenco veicoli  4) Elenco utenti");
            System.out.println("5) Sblocca veicolo   6) Termina noleggio  7) Ricarica credito  8) Metti in ricarica");
            System.out.println("9) Completa ricarica 10) Storico noleggi  11) Elimina veicolo  0) Esci");
            try {
                switch (leggi("Scelta: ")) {
                    case "1" -> {
                        int tipo = intero("1=Bici, 2=Monopattino: ");
                        int id = intero("Codice: ");
                        int b = intero("Batteria (0-100): ");
                        flotta.registraVeicolo(tipo == 1 ? new Bici(id, b, "disponibile") : tipo == 2 ? new Monopattino(id, b, "disponibile") : throwTipo());
                        System.out.println("Veicolo registrato");
                    }
                    case "2" -> {
                        int id = intero("ID: ");
                        String nome = leggi("Nome: ");
                        String cf = leggi("Codice fiscale: ");
                        double credito = decimale("Credito EUR: ");
                        String email = leggi("Email: ");
                        flotta.registraUtente(new Utente(id, nome, cf, credito, email.toCharArray()));
                        System.out.println("Utente registrato");
                    }
                    case "3" -> flotta.elencoVeicoli().forEach(System.out::println);
                    case "4" -> flotta.elencoUtenti().forEach(System.out::println);
                    case "5" -> {
                        int codice = intero("Codice veicolo: ");
                        int id = intero("ID utente: ");
                        int min = intero("Minuti previsti: ");
                        System.out.println(flotta.SbloccaVeicolo(codice, min, flotta.cercaId(id), id));
                    }
                    case "6" -> flotta.terminaNoleggio(intero("Codice veicolo: "), intero("Minuti effettivi: "));
                    case "7" -> {
                        flotta.ricaricaCredito(intero("ID utente: "), decimale("Importo EUR: "));
                        System.out.println("Credito aggiornato");
                    }
                    case "8" -> {
                        flotta.mettiInRicarica(leggi("Codice veicolo: "));
                        System.out.println("In ricarica");
                    }
                    case "9" -> {
                        flotta.completaRicarica(intero("Codice veicolo: "));
                        System.out.println("Batteria al 100%");
                    }
                    case "10" -> flotta.elencoNoleggi().forEach(System.out::println);
                    case "11" -> {
                        flotta.eliminaVeicolo(intero("Codice veicolo: "));
                        System.out.println("Veicolo eliminato");
                    }
                    case "0" -> {
                        System.out.println("Arrivederci!");
                        return;
                    }
                    default -> System.out.println("Scelta non valida");
                }
            } catch (IllegalArgumentException | RossiGoException e) {
                System.out.println("Attenzione: " + e.getMessage());
            }
        }
    }

    private static Veicolo throwTipo() {
        throw new IllegalArgumentException("Tipo non valido");
    }
}
