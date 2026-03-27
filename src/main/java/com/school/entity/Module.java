package com.school.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="modules") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Module {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String nom;
    @Column(nullable=false) private String niveau;
    @Column(name="nombre_heures", nullable=false) @Builder.Default private Integer nombreHeures=0;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="professeur_id") private Professeur professeur;
}
