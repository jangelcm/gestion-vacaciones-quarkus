package org.acme.resource;


import org.acme.commons.PaginationModel;
import org.acme.dto.UpdateUserRequest;
import org.acme.dto.UserResponseDto;
import org.acme.services.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService service;

    @POST
    @Path("/pagination")
    public Response pagination(PaginationModel paginationModel) throws ReflectiveOperationException{
        System.out.println("paginationModel: " + paginationModel);
        //return Response.ok( service.paginationProjections(paginationModel) ).build();
        return Response.ok( service.paginationProjections(paginationModel) ).build();
    }

    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Long id) {
        var dto = service.obtenerPorId(id);
        if (dto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(dto).build();
    }

    @GET
    @Path("/rol/{rol}")
    public List<UserResponseDto> listarPorRol(@PathParam("rol") String rol) {
        return service.listarPorRol(rol);
    }

    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") Long id, UpdateUserRequest req) {
        try {
            return Response.ok(service.actualizar(id, req)).build();
        } catch (NoSuchElementException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

}
