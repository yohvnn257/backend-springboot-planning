package com.school.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String titre;
    @Column(columnDefinition="TEXT") private String description;
    @Column(nullable=false) @Builder.Default private String type="BUG";
    @Column private String email;
    @Column(nullable=false) @Builder.Default private String statut="OUVERT";
    @Column(name="date_creation",nullable=false) @Builder.Default private LocalDateTime dateCreation=LocalDateTime.now();
    @Column(name="date_resolution") private LocalDateTime dateResolution;
}
