/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon.util;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author bruno
 */
public class LocalDateISO8601Deserializer extends LocalDateDeserializer{
    
    public LocalDateISO8601Deserializer(){
        super(DateTimeFormatter.ISO_INSTANT);
    }
    
}
