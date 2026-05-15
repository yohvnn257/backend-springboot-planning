package com.school.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="professeurs") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Professeur {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String nom;
    @Column(nullable=false, unique=true) private String email;
    @Column private String telephone;
    @Column(name="whatsapp_statut", nullable=false) @Builder.Default private String whatsappStatut="ATTENTE";
    // Secret : ne jamais exposer via /api/professeurs ou autres endpoints REST.
    // Les endpoints qui doivent fournir le lien personnel construisent l'URL explicitement (EmailService).
    @JsonIgnore @Column(name="response_token") private String responseToken;
    @JsonIgnore @Column(name="token_expire_at") private LocalDateTime tokenExpireAt;
    @JsonIgnore @OneToMany(mappedBy="professeur", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    @Builder.Default private List<Disponibilite> disponibilites=new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="professeur", fetch=FetchType.LAZY)
    @Builder.Default private List<Module> modules=new ArrayList<>();
}
