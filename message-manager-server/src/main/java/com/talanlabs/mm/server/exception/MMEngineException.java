package com.talanlabs.mm.server.exception;

public class MMEngineException extends RuntimeException {

    public MMEngineException() {
        super("The mm engine has not been initialized correctly");
    }
}
