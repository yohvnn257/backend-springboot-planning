package com.school.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="utilisateurs") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Utilisateur {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String username;
    @JsonIgnore @Column(name="password_hash", nullable=false) private String passwordHash;
    @Column(name="must_change_password", nullable=false) @Builder.Default private Boolean mustChangePassword=true;
    @Column(name="created_at", nullable=false) @Builder.Default private LocalDateTime createdAt=LocalDateTime.now();
}
