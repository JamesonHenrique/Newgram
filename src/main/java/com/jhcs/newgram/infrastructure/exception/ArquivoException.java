package com.jhcs.newgram.infrastructure.exception;


public class ArquivoException extends RuntimeException {
    public ArquivoException(String mensagem) {
        super(mensagem);
    }

    public ArquivoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}