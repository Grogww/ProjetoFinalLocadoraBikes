package br.edu.ifc.bikes.exception;

public class CartaoVencidoException extends RuntimeException{
    public CartaoVencidoException(String message){
        super(message);
    }
}
