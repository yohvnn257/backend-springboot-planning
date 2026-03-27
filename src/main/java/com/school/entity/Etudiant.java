package com.school.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="etudiants") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Etudiant {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String nom;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String filiere;
    @Column(nullable=false) private String niveau;
}
