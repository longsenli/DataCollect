package com.tnpy.datacollector.serial;

public class SerialPortException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private String message;

    public SerialPortException(String message) {
    	this.message=message;
    }

    @Override
    public String toString() {
        return message;
    }
}