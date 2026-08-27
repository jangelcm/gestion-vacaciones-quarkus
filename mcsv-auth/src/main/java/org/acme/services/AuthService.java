
package org.acme.services;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.acme.models.RolsUser;
import org.acme.models.User;
import org.acme.repository.RolUserRepository;
import org.acme.repository.RolsRepository;
import org.acme.repository.UserRepository;
import org.acme.view.UserRolView;
import org.mindrot.jbcrypt.BCrypt;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
@ApplicationScoped

public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @ConfigProperty(name = "jwt.issuer")
    String jwtIssuer;

    @Inject
    UserRepository userRepository;

    @Inject
    RolsRepository rolsRepository;

    @Inject
    RolUserRepository rolUserRepository;

    @Inject
    EntityManager entityManager;

    @Transactional
    public User register(String username, String password, String roles) {
        User user = new User();
        user.username = username;
        user.passwordHash = hashPassword(password);
        userRepository.persist(user);
        return user;
    }

    public User validateCredentials(String username, String password) {
        
        log.info("Validating credentials for user: {}", username);

        User user = userRepository.findByUsername(username);
        log.info("User found: {}", user != null ? user.username : "null");
        if (user != null && verifyPassword(password, user.passwordHash)) {
            log.info("Password verified for user: {}", username);
            return user;
        }
        log.info("Invalid credentials for user: {}", username);
        return null;
    }

    public String generateAccessToken(User user) {
        Set<String> rolesSet = new HashSet<>();
        rolesSet = getRolesByUserId(user.id);

        // if (user.id != null) {
        //     for (Rols rol : rolsRepository.findByIdUser(user.id)) {
        //         if (rol.descripcion != null) {
        //             String descripcion = rol.descripcion.trim();
        //             if (!descripcion.isEmpty()) {
        //                 rolesSet.add(descripcion);
        //             }
        //         }
        //     }
        // }
        return Jwt.issuer(jwtIssuer)
                .subject(user.username)
                .upn(user.username)
                .groups(rolesSet)
                .expiresIn(Duration.ofHours(1))
                .sign();
    }

    @Transactional
    public String generateRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        user.refreshToken = refreshToken;
        //entityManager.merge(user);
        return refreshToken;
    }

    public boolean validateRefreshToken(User user, String refreshToken) {
        return user.refreshToken != null && user.refreshToken.equals(refreshToken);
    }

    // Métodos de hash y verificación de contraseña (BCrypt)
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean verifyPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    
    Set<String> getRolesByUserId(Long userId) {
        Set<String> rolesSet = new HashSet<>();

        // List<RolsUser> rolUsers = rolUserRepository.findByUserId(userId);
        List<UserRolView> userRolViews = rolUserRepository.findUserRolViews(userId);

        for(UserRolView userRolView : userRolViews) {
            if (userRolView.rolName() != null) {
                String rolName = userRolView.rolName().trim();
                if (!rolName.isEmpty()) {
                    rolesSet.add(rolName);
                }
            }
        }

        // for (RolsUser rolUser : rolUsers) {
        //     if (rolUser.rol != null && rolUser.rol.descripcion != null) {
        //         String descripcion = rolUser.rol.descripcion.trim();
        //         if (!descripcion.isEmpty()) {
        //             rolesSet.add(descripcion);
        //         }
        //     }
        // }
        return rolesSet;
    }
}
