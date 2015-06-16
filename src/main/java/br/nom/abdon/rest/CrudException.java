package br.nom.abdon.rest;

import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
class CrudException extends Exception{

    private Response.Status status;
    private ErrorPojo errorPojo;

    protected CrudException(
        Response.Status status,
        String message,
        Throwable causa) {

        super(causa);
        this.status = status;
        this.errorPojo = new ErrorPojo(message);
    }
 
    protected CrudException(Response.Status status, String message) {
        this(status, message, null);
}
    
    public Response.Status getStatus() {
        return status;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }

    public ErrorPojo getErrorPojo() {
        return errorPojo;
    }

    public void setErrorPojo(ErrorPojo errorPojo) {
        this.errorPojo = errorPojo;
    }


}
