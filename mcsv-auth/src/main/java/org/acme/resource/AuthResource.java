
package org.acme.resource;

import org.acme.models.User;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.repository.UserRepository;
import org.acme.services.AuthService;

import java.util.HashMap;
import java.util.Map;
import org.acme.dto.RegisterRequest;
import org.acme.dto.LoginRequest;
import org.acme.dto.RefreshRequest;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    AuthService authService;
    @Inject
    UserRepository userRepository;
    
    @Inject
    JsonWebToken jsonWebToken;

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest req) {
        String username = req.username;
        String password = req.password;
        //String roles = req.roles != null ? req.roles : "USER";
        if (userRepository.findByUsername(username) != null) {
            return Response.status(Response.Status.CONFLICT).entity("Usuario ya existe").build();
        }
        User user = authService.register(username, password, "");
        return Response.ok().entity(user).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest req) {
        String username = req.username;
        String password = req.password;
        User user = authService.validateCredentials(username, password);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciales inválidas").build();
        }
        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        return Response.ok(tokens).build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(RefreshRequest req) {
        String username = req.username;
        String refreshToken = req.refresh_token;
        User user = userRepository.findByUsername(username);
        if (user == null || !authService.validateRefreshToken(user, refreshToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Refresh token inválido").build();
        }
        String newAccessToken = authService.generateAccessToken(user);
        String newRefreshToken = authService.generateRefreshToken(user);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);
        tokens.put("refresh_token", newRefreshToken);
        return Response.ok(tokens).build();
    }

    @GET
    @Path("/validate")
    @Authenticated
    public Response validate() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("valid", true);
        payload.put("username", jsonWebToken.getName());
        payload.put("issuer", jsonWebToken.getIssuer());
        payload.put("subject", jsonWebToken.getSubject());
        payload.put("groups", jsonWebToken.getGroups());
        payload.put("expires_at", jsonWebToken.getExpirationTime());
        return Response.ok(payload).build();
    }
}
