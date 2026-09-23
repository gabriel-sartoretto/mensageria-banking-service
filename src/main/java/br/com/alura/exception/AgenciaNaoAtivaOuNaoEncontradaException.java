package br.com.alura.exception;

public class AgenciaNaoAtivaOuNaoEncontradaException extends RuntimeException {

    public AgenciaNaoAtivaOuNaoEncontradaException() {
        super("Agência não encontrada ou não está ativa");
    }
}
