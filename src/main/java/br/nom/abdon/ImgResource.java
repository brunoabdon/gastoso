/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.nom.abdon;

import java.io.File;
import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author bruno
 */
@Path("img")

public class ImgResource {

    @GET
    @Produces("image/*")
    public Response getImage() {
        File f = new File("/home/bruno/Images/fotos/eu/brunoabdon3x4.JPG");

        if (!f.exists()) {
            throw new WebApplicationException(404);
        }

        String mt = new MimetypesFileTypeMap().getContentType(f);
        return Response.ok(f, "image/png").build();
    }

    
}
