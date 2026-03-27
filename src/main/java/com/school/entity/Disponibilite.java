package com.school.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
@Entity @Table(name="disponibilites") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Disponibilite {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="professeur_id", nullable=false) private Professeur professeur;
    @Column(nullable=false) private String filiere;
    @Column(nullable=false) private String niveau;
    @Column private String salle;
    @Column(nullable=false) private LocalDate jour;
    @Column(name="heure_debut", nullable=false) private LocalTime heureDebut;
    @Column(name="heure_fin", nullable=false) private LocalTime heureFin;
    @Column(nullable=false) @Builder.Default private String statut="DISPONIBLE";
}
