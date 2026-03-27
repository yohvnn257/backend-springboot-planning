package com.school.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity @Table(name="emplois_du_temps") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmploiDuTemps {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String filiere;
    @Column(nullable=false) private String niveau;
    @Column(name="semaine_du", nullable=false) private LocalDate semaineDu;
    @Column(name="semaine_au", nullable=false) private LocalDate semaineAu;
    @Column(name="creneaux_json", columnDefinition="TEXT") private String creneauxJson;
    @Column(nullable=false) @Builder.Default private String statut="GENERE";
    @Column(name="date_creation", nullable=false) @Builder.Default private LocalDateTime dateCreation=LocalDateTime.now();
}
