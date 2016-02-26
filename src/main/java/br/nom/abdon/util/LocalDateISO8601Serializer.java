package br.nom.abdon.util;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author bruno
 */
public class LocalDateISO8601Serializer extends LocalDateSerializer{
    
    public static final LocalDateISO8601Serializer INSTANCE = 
        new LocalDateISO8601Serializer();
    
    public LocalDateISO8601Serializer(){
        super(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    
}
