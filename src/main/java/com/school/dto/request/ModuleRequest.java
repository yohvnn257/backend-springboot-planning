package com.school.dto.request;
import lombok.Data;
@Data
public class ModuleRequest {
    private String nom;
    private String niveau;
    private Integer nombreHeures;
    private String salle;
    private Long professeurId;
}
