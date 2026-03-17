package com.gilson.br.cadastro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "O e-mail introduzido já se encontra registado")
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String message) {
        super(message);
    }
}