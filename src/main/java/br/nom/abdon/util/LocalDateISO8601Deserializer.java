package br.nom.abdon.util;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author bruno
 */
public class LocalDateISO8601Deserializer extends LocalDateDeserializer{
    
    public LocalDateISO8601Deserializer(){
        super(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
