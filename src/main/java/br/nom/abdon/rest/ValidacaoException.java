/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon.rest;

import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 */
public class ValidacaoException extends CrudException {

    public ValidacaoException(String message) {
        super(Response.Status.BAD_REQUEST,"Erro de Validação. " + message);
    }
}
