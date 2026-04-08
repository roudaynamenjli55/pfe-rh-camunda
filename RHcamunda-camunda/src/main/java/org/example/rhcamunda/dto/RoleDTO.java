package org.example.rhcamunda.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleDTO {

    private Long id;
    private String code; // EMPLOYE, CHEF_HIERARCHIQUE, RH, ADMIN
    private Boolean employe;
    private Boolean chefHierarchique;
    private Boolean rh;
    private Boolean administrateur;
}