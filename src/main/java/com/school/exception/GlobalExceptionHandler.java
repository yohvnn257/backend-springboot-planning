package com.school.exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("erreur",e.getMessage(),"status",404,"timestamp",LocalDateTime.now().toString()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(Map.of("erreur",e.getMessage(),"status",400,"timestamp",LocalDateTime.now().toString()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> general(Exception e) {
        log.error("Erreur interne",e);
        return ResponseEntity.status(500).body(Map.of("erreur","Erreur interne. Consultez les logs.","status",500,"timestamp",LocalDateTime.now().toString()));
    }
}
