package com.school.dto.request;
import lombok.Data;
import java.time.LocalDate;
@Data
public class GenererEdtRequest {
    private String filiere;
    private String niveau;
    private LocalDate semaineDu;
    private LocalDate semaineAu;
}
