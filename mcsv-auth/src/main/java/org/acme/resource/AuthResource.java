
package org.acme.resource;

import org.acme.models.User;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
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
import org.acme.exception.ConflictException;
import org.acme.exception.ResourceUnAuthorizedException;

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
    public Response register(RegisterRequest req) {
        String username = req.username;
        String password = req.password;
        if (userRepository.findByUsername(username) != null) {
            throw new ConflictException("Usuario ya existe");
        }
        User user = authService.register(username, password, req.email, req.rol, req.fechaIngreso);
        return Response.ok().entity(user).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest req) {
        String username = req.username;
        String password = req.password;
        User user = authService.validateCredentials(username, password);
        if (user == null) {
            throw new ResourceUnAuthorizedException("Credenciales inválidas");
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
            throw new ResourceUnAuthorizedException("Refresh token inválido");
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
