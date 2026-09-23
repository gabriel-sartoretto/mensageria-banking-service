package br.com.alura.exception;

public class AgenciaJaExistenteException extends RuntimeException {

    public AgenciaJaExistenteException() {
        super("Agência já cadastrada!");
    }
}
