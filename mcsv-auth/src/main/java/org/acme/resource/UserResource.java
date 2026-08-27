package org.acme.resource;


import org.acme.commons.PaginationModel;
import org.acme.services.UserService;


import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/users")
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

}
