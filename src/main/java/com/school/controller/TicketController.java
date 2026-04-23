package com.school.controller;
import com.school.entity.Ticket;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/tickets") @RequiredArgsConstructor
public class TicketController {
    private final TicketRepository repo;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAll(@RequestParam(required=false) String statut) {
        return ResponseEntity.ok(statut!=null ? repo.findByStatut(statut.toUpperCase()) : repo.findAllByOrderByDateCreationDesc());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String,Object>> stats() {
        return ResponseEntity.ok(Map.of(
            "total",repo.count(),
            "ouverts",repo.findByStatut("OUVERT").size(),
            "enCours",repo.findByStatut("EN_COURS").size(),
            "resolus",repo.findByStatut("RESOLU").size()
        ));
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@RequestBody Map<String,String> body) {
        return ResponseEntity.status(201).body(repo.save(Ticket.builder()
            .titre(body.getOrDefault("titre","Sans titre"))
            .description(body.get("description"))
            .type(body.getOrDefault("type","BUG").toUpperCase())
            .email(body.get("email"))
            .build()));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Ticket> updateStatut(@PathVariable Long id, @RequestBody Map<String,String> body) {
        Ticket t=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Ticket",id));
        t.setStatut(body.get("statut").toUpperCase());
        if("RESOLU".equals(t.getStatut())) t.setDateResolution(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if(!repo.existsById(id)) throw new ResourceNotFoundException("Ticket",id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
