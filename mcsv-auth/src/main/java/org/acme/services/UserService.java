package org.acme.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.acme.commons.PageResponse;
import org.acme.commons.PageableCommons;
import org.acme.commons.PaginationModel;
import org.acme.commons.SpecificationProjectionUtil;
import org.acme.dto.UserResponseDto;
import org.acme.models.User;



import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;


@ApplicationScoped
@Transactional
public class UserService {

    @Inject
    EntityManager entityManager;

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
