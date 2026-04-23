package com.school.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class ClaudeService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${anthropic.api.key:}") private String apiKey;
    @Value("${anthropic.api.url}") private String apiUrl;
    @Value("${anthropic.model}") private String model;

    public Map<String,Object> optimiserEdt(String filiere, String niveau, List<Map<String,Object>> creneaux) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of("creneauxOptimises", creneaux,
                "suggestions", "IA non configurée — ajoutez ANTHROPIC_API_KEY dans Render.",
                "iaActive", false);
        }
        try {
            String creneauxJson = objectMapper.writeValueAsString(creneaux);
            String prompt = "Tu es un expert en planification scolaire.\n" +
                "Filière : " + filiere + " — Niveau : " + niveau + "\n" +
                "Créneaux disponibles :\n" + creneauxJson + "\n\n" +
                "Optimise l'emploi du temps : max 2 cours/jour, équilibrer la semaine, préférer le matin.\n" +
                "Réponds UNIQUEMENT en JSON valide sans markdown :\n" +
                "{\"creneauxOptimises\": [...], \"suggestions\": \"...\"}";

            Map<String,Object> body = Map.of("model", model, "max_tokens", 1500,
                "messages", List.of(Map.of("role","user","content", prompt)));
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.set("x-api-key", apiKey);
            h.set("anthropic-version", "2023-06-01");

            ResponseEntity<Map> resp = restTemplate.exchange(apiUrl, HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> content = (List<Map<String,Object>>) resp.getBody().get("content");
            @SuppressWarnings("unchecked")
            Map<String,Object> result = objectMapper.readValue((String) content.get(0).get("text"), Map.class);
            result.put("iaActive", true);
            return result;
        } catch (Exception e) {
            log.error("Erreur Claude API: {}", e.getMessage());
            return Map.of("creneauxOptimises", creneaux, "suggestions", "Erreur IA: " + e.getMessage(), "iaActive", false);
        }
    }

    public Map<String,Object> analyserDisponibilites(String filiere, String niveau, List<Map<String,Object>> dispos) {
        if (apiKey == null || apiKey.isBlank())
            return Map.of("analyse","IA non configurée","suffisant",false,"iaActive",false);
        try {
            String prompt = "Analyse ces disponibilités pour " + filiere + " " + niveau + " :\n" +
                objectMapper.writeValueAsString(dispos) + "\n\n" +
                "Réponds en JSON : {\"suffisant\":true/false,\"nombreCreneaux\":N,\"joursCouverts\":[...],\"analyse\":\"...\",\"recommandations\":\"...\"}";
            Map<String,Object> body = Map.of("model",model,"max_tokens",500,
                "messages", List.of(Map.of("role","user","content",prompt)));
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.set("x-api-key", apiKey);
            h.set("anthropic-version","2023-06-01");
            ResponseEntity<Map> resp = restTemplate.exchange(apiUrl,HttpMethod.POST,new HttpEntity<>(body,h),Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> content=(List<Map<String,Object>>)resp.getBody().get("content");
            @SuppressWarnings("unchecked")
            Map<String,Object> result=objectMapper.readValue((String)content.get(0).get("text"),Map.class);
            result.put("iaActive",true);
            return result;
        } catch(Exception e) {
            log.error("Erreur analyse: {}",e.getMessage());
            return Map.of("analyse","Erreur: "+e.getMessage(),"iaActive",false);
        }
    }
}
