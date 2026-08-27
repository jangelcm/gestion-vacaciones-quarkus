package org.acme.resource;

import java.util.List;

import org.acme.models.Rols;
import org.acme.services.RolsService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/rols")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RolsResource {

    @Inject
    RolsService rolsService;

    @GET
    public Response getAll() {
        List<Rols> rols = rolsService.getAll();
        return Response.ok(rols).build();
    }

    @GET
    @Path("/user/{idUser}")
    public Response getByIdUser(@PathParam("idUser") Long idUser) {
        List<Rols> rols = rolsService.getByIdUser(idUser);
        return Response.ok(rols).build();
    }

    @POST
    public Response create(Rols rol) {
        Rols created = rolsService.create(rol);
        return Response.ok(created).build();
    }
}