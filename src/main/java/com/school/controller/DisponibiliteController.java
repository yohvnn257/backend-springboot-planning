package com.school.controller;
import com.school.dto.response.ApiResponse;
import com.school.entity.Disponibilite;
import com.school.service.DisponibiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/api/disponibilites") @RequiredArgsConstructor
public class DisponibiliteController {
    private final DisponibiliteService svc;
    @GetMapping public ResponseEntity<List<Disponibilite>> getAll(){return ResponseEntity.ok(svc.findAll());}
    @GetMapping("/semaine") public ResponseEntity<List<Disponibilite>> semaine(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate debut,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin){return ResponseEntity.ok(svc.findBySemaine(debut,fin));}
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse> delete(@PathVariable Long id){svc.delete(id);return ResponseEntity.ok(ApiResponse.ok("Supprimé"));}
}
