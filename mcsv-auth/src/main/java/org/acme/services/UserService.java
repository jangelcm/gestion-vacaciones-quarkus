package org.acme.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.acme.commons.PageResponse;
import org.acme.commons.PageableCommons;
import org.acme.commons.PaginationModel;
import org.acme.commons.SpecificationProjectionUtil;
import org.acme.dto.UpdateUserRequest;
import org.acme.dto.UserResponseDto;
import org.acme.models.User;
import org.acme.repository.UserRepository;

import java.util.NoSuchElementException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;


@ApplicationScoped
@Transactional
public class UserService {

    @Inject
    EntityManager entityManager;

    @Inject
    UserRepository userRepository;

    /**
     * Uso interno servicio-a-servicio (ej. ms-notificaciones difundiendo "nueva solicitud
     * pendiente" a todos los aprobadores), igual patron que obtenerPorId: sin JWT, llamado
     * directo entre contenedores.
     */
    public List<UserResponseDto> listarPorRol(String rol) {
        List<User> usuarios = entityManager
                .createQuery("SELECT ru.user FROM RolsUser ru WHERE ru.rol.descripcion = :rol", User.class)
                .setParameter("rol", rol)
                .getResultList();

        return usuarios.stream()
                .map(user -> UserResponseDto.builder()
                        .id(user.id)
                        .username(user.username)
                        .email(user.email)
                        .telefono(user.telefono)
                        .isActive(user.isActive)
                        .build())
                .toList();
    }

    public UserResponseDto obtenerPorId(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.id)
                .username(user.username)
                .email(user.email)
                .telefono(user.telefono)
                .isActive(user.isActive)
                .build();
    }

    @Transactional
    public UserResponseDto actualizar(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new NoSuchElementException("Usuario no encontrado con id: " + id);
        }

        if (req.username != null && !req.username.isBlank() && !req.username.equals(user.username)) {
            User existente = userRepository.findByUsername(req.username);
            if (existente != null && !existente.id.equals(id)) {
                throw new IllegalStateException("Ya existe otro usuario con el username: " + req.username);
            }
            user.username = req.username;
        }
        if (req.email != null) {
            user.email = req.email.isBlank() ? null : req.email.trim();
        }
        if (req.telefono != null) {
            user.telefono = req.telefono.isBlank() ? null : req.telefono.trim();
        }
        if (req.isActive != null) {
            user.isActive = req.isActive;
        }

        return UserResponseDto.builder()
                .id(user.id)
                .username(user.username)
                .email(user.email)
                .telefono(user.telefono)
                .isActive(user.isActive)
                .build();
    }

    @Transactional
    public PageResponse<?> paginationProjections(PaginationModel paginationModel){

        String join = "";

        String jpql =
        "SELECT " +
        "p.id, p.username, p.email, p.telefono, p.isActive" +
        " FROM User p";   

        List<String> fieldsDto = List.of("id", "username", "email", "telefono", "isActive");
     
        Map<String, Object> filtersMap = new HashMap<>();
        if(paginationModel.getFilters() != null && !paginationModel.getFilters().isEmpty()){
            filtersMap = (Map<String, Object>) paginationModel.getFiltersMap() ;
        }

        // si queremos actualizar 
        if(filtersMap.containsKey("idExample")){  
            Map<String, Object> datos = (Map<String, Object>) filtersMap.get("id");
            datos.put("columnName", "p.id");
          
        }

        Map<String, Object> sorts = paginationModel.getSortMap() ;

        PageableCommons pageable = PageableCommons.of(paginationModel.getPageNumber(), paginationModel.getRowsPerPage());

        PageResponse<UserResponseDto> projectionResult 
             = SpecificationProjectionUtil.filterSortProjection(
              entityManager,
              User.class, 
              jpql, 
              join, 
              fieldsDto,
             filtersMap, 
             sorts, 
             pageable,
             UserResponseDto.class
         );

        return projectionResult;
    }

}
