package br.edu.ifc.bikes.exception;

public class LocacaoFinalizadaException extends RuntimeException{
    public LocacaoFinalizadaException(String message){
        super(message);
    }
}
