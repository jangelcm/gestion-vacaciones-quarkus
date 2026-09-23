// package org.acme.config;

// import java.time.LocalDate;
// import java.util.List;

// import org.acme.models.Rols;
// import org.acme.models.RolsUser;
// import org.acme.models.User;
// import org.acme.repository.RolUserRepository;
// import org.acme.repository.RolsRepository;
// import org.acme.repository.UserRepository;
// import org.eclipse.microprofile.config.inject.ConfigProperty;
// import org.jboss.logging.Logger;
// import org.mindrot.jbcrypt.BCrypt;

// import io.quarkus.runtime.StartupEvent;
// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.enterprise.event.Observes;
// import jakarta.transaction.Transactional;

// @ApplicationScoped
// public class DefaultUsersSeeder {

//     private static final Logger LOG = Logger.getLogger(DefaultUsersSeeder.class);

//     @ConfigProperty(name = "app.seeder.enabled", defaultValue = "false")
//     boolean seederEnabled;

//     private static final List<String> REQUIRED_ROLES = List.of(
//             "Colaborador",
//             "Jefe Inmediato",
//             "Recursos Humanos",
//             "Administrador");

//     private static final List<DefaultUser> DEFAULT_USERS = List.of(
//             new DefaultUser("admin", "admin123", "admin@vacaciones.local", "Administrador"),
//             new DefaultUser("rh", "rh12345", "rh@vacaciones.local", "Recursos Humanos"),
//             new DefaultUser("jefe", "jefe123", "jefe@vacaciones.local", "Jefe Inmediato"));

//     private final UserRepository userRepository;
//     private final RolsRepository rolsRepository;
//     private final RolUserRepository rolUserRepository;

//     public DefaultUsersSeeder(UserRepository userRepository, RolsRepository rolsRepository,
//             RolUserRepository rolUserRepository) {
//         this.userRepository = userRepository;
//         this.rolsRepository = rolsRepository;
//         this.rolUserRepository = rolUserRepository;
//     }

//     @Transactional
//     void onStart(@Observes StartupEvent event) {
//         if (!seederEnabled) {
//             LOG.info("Seeder de usuarios omitido (app.seeder.enabled=false).");
//             return;
//         }

//         ensureRolesExist();

//         for (DefaultUser defaultUser : DEFAULT_USERS) {
//             createUserIfMissing(defaultUser);
//         }
//     }

//     private void ensureRolesExist() {
//         for (String roleDescription : REQUIRED_ROLES) {
//             if (rolsRepository.findByDescripcion(roleDescription) != null) {
//                 continue;
//             }

//             Rols role = new Rols();
//             role.descripcion = roleDescription;
//             rolsRepository.persist(role);
//             LOG.infof("Rol por defecto '%s' creado por DefaultUsersSeeder", roleDescription);
//         }
//     }

//     private void createUserIfMissing(DefaultUser defaultUser) {
//         User existingUser = userRepository.findByUsername(defaultUser.username());
//         if (existingUser != null) {
//             LOG.infof("Usuario '%s' ya existe, se omite seed", defaultUser.username());
//             return;
//         }

//         Rols role = rolsRepository.findByDescripcion(defaultUser.rolDescripcion());
//         if (role == null) {
//             LOG.warnf("No se pudo crear usuario '%s': rol '%s' no existe",
//                     defaultUser.username(), defaultUser.rolDescripcion());
//             return;
//         }

//         User user = new User();
//         user.username = defaultUser.username();
//         user.passwordHash = BCrypt.hashpw(defaultUser.password(), BCrypt.gensalt());
//         user.email = defaultUser.email();
//         user.isActive = true;
//         user.fechaIngreso = LocalDate.now().minusYears(1);
//         userRepository.persist(user);

//         RolsUser rolsUser = new RolsUser();
//         rolsUser.user = user;
//         rolsUser.rol = role;
//         rolUserRepository.persist(rolsUser);

//         LOG.infof("Usuario por defecto '%s' creado con rol '%s'", defaultUser.username(), defaultUser.rolDescripcion());
//     }

//     private record DefaultUser(String username, String password, String email, String rolDescripcion) {
//     }
// }