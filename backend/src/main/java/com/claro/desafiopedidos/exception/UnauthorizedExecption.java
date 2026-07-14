package com.claro.desafiopedidos.exception;

public class UnauthorizedExecption extends RuntimeException {
    public UnauthorizedExecption(String message) {
        super(message);
    }
}
