/*
 * Copyright (C) 2016 Bruno Abdon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.nom.abdon.gastoso.rest.client;


/**
 *
 * @author Bruno Abdon
 */
public class RESTClientRTException extends RuntimeException {

    public static final int ERRO_GERAL = 0;
    public static final int SERVIDOR_FORA = 1;
    public static final int SERVIDOR_DESNORTEADO = 2;
    
    private int code;

    public RESTClientRTException(final Throwable cause, final int errorCode){
        super(cause);
        this.code = errorCode;
    }

    public RESTClientRTException(final String message, final int errorCode) {
        super(message);
        this.code = errorCode;
    }

    public RESTClientRTException(final String message, final Throwable e) {
        this(message,e,ERRO_GERAL);
    }

    public RESTClientRTException(Throwable e) {
        this(e,ERRO_GERAL);
    }

    public RESTClientRTException(
            final String message,
            final Throwable cause,
            final int errorCode) {
        super(message, cause);
        this.code = errorCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }    
    
}
