package com.school.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Service IA basé sur Google Gemini 3 Flash (fallback Gemini 2.5 Flash via env GEMINI_MODEL). */
@Service @RequiredArgsConstructor @Slf4j
public class IaService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${gemini.api.key:}") private String apiKey;
    @Value("${gemini.api.url}") private String apiUrl;
    @Value("${gemini.model}") private String model;

    public Map<String,Object> optimiserEdt(String filiere, String niveau, String semaineDu, String semaineAu, List<Map<String,Object>> creneaux) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.ofEntries(
                Map.entry("creneauxOptimises", creneaux),
                Map.entry("edtTemplate", fallbackEdtTemplate(filiere, niveau, semaineDu, semaineAu, creneaux)),
                Map.entry("suggestions", "IA non configurée — ajoutez GEMINI_API_KEY dans Render."),
                Map.entry("iaActive", false)
            );
        }
        try {
            String creneauxJson = objectMapper.writeValueAsString(creneaux);
            String prompt = buildEdtPrompt(filiere, niveau, semaineDu, semaineAu, creneauxJson);
            Map<String,Object> response = appelerGemini(prompt, 4096, creneaux, "optimiserEdt");
            response.putIfAbsent("edtTemplate", fallbackEdtTemplate(filiere, niveau, semaineDu, semaineAu, creneaux));
            response.putIfAbsent("creneauxOptimises", creneaux);
            return response;
        } catch (Exception e) {
            log.error("Erreur Gemini API (optimiserEdt): {}", e.getMessage());
            return Map.ofEntries(
                Map.entry("creneauxOptimises", creneaux),
                Map.entry("edtTemplate", fallbackEdtTemplate(filiere, niveau, semaineDu, semaineAu, creneaux)),
                Map.entry("suggestions", "Erreur IA: " + e.getMessage()),
                Map.entry("iaActive", false)
            );
        }
    }

    public Map<String,Object> analyserDisponibilites(String filiere, String niveau, List<Map<String,Object>> dispos) {
        if (apiKey == null || apiKey.isBlank())
            return Map.of("analyse","IA non configurée","suffisant",false,"iaActive",false);
        try {
            String prompt = "Analyse ces disponibilités pour " + filiere + " " + niveau + " :\n" +
                objectMapper.writeValueAsString(dispos) + "\n\n" +
                "Réponds en JSON : {\"suffisant\":true/false,\"nombreCreneaux\":N,\"joursCouverts\":[...],\"analyse\":\"...\",\"recommandations\":\"...\"}";
            return appelerGemini(prompt, 800, null, "analyserDisponibilites");
        } catch(Exception e) {
            log.error("Erreur Gemini API (analyserDisponibilites): {}",e.getMessage());
            return Map.of("analyse","Erreur: "+e.getMessage(),"iaActive",false);
        }
    }

    /** Appel HTTP générique vers l'API Gemini avec mode JSON natif. */
    @SuppressWarnings({"unchecked","rawtypes"})
    private Map<String,Object> appelerGemini(String prompt, int maxTokens, List<Map<String,Object>> fallback, String contexte) throws Exception {
        Map<String,Object> body = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "maxOutputTokens", maxTokens,
                "temperature", 0.3
            )
        );
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-goog-api-key", apiKey);
        String url = apiUrl.replace("{model}", model);

        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
        List<Map<String,Object>> candidates = (List<Map<String,Object>>) resp.getBody().get("candidates");
        if (candidates == null || candidates.isEmpty())
            throw new RuntimeException("Réponse Gemini vide");
        Map<String,Object> content = (Map<String,Object>) candidates.get(0).get("content");
        List<Map<String,Object>> parts = (List<Map<String,Object>>) content.get("parts");
        String texte = (String) parts.get(0).get("text");

        Map<String,Object> result = objectMapper.readValue(texte, Map.class);
        result.put("iaActive", true);
        log.debug("Gemini {} OK ({} chars)", contexte, texte.length());
        return result;
    }

    private String buildEdtPrompt(String filiere, String niveau, String semaineDu, String semaineAu, String creneauxJson) {
        return """
Tu es un planificateur académique expert de l'Institut Supérieur du Digital.
Objectif: générer un EDT hebdomadaire STRICTEMENT exploitable par un template HTML n8n.

Contexte:
- Filière: %s
- Niveau: %s
- Semaine du: %s
- Semaine au: %s
- Données source (créneaux disponibles): %s

Contraintes métier:
1) Respecter uniquement les créneaux fournis.
2) Pas de collision d'horaires pour une même journée.
3) Équilibrer la semaine (éviter surcharge d'un seul jour).
4) Préserver les informations pédagogiques: module/matière, professeur, salle.
5) Produire des fusions verticales via rowSpan quand un cours couvre plusieurs lignes horaires.

Contraintes de sortie:
- Réponds UNIQUEMENT en JSON valide (sans markdown, sans commentaire, sans texte hors JSON).
- Toutes les dates au format YYYY-MM-DD.
- Toutes les heures au format HH:mm.
- Le tableau doit pouvoir afficher exactement:
  * En-tête logo "Institut Supérieur du Digital"
  * Titre "EMPLOI DU TEMPS (27 - 30 AVRIL 2026)" (avec les dates de la semaine demandée)
  * Sous-titre "LICENCE 3 : DEVELOPPEMENT WEB" (adapter avec niveau/filière)
  * Colonnes Heures + Lundi..Vendredi (date incluse)
  * Séparateur matin/après-midi (fond bleu)
  * Note bas de page en rouge

Schéma JSON attendu:
{
  "suggestions": "analyse courte et actionnable",
  "creneauxOptimises": [
    {
      "jour": "YYYY-MM-DD",
      "heureDebut": "HH:mm",
      "heureFin": "HH:mm",
      "module": "string",
      "matiere": "string",
      "professeur": "string",
      "salle": "string"
    }
  ],
  "edtTemplate": {
    "institution": "Institut Supérieur du Digital",
    "titre": "EMPLOI DU TEMPS (...)",
    "sousTitre": "NIVEAU : FILIERE",
    "noteBasPage": "NB : L'emploi du temps n'est pas définitif. Il peut toujours subir des modifications.",
    "colonnes": [
      "Heures",
      "Lundi DD/MM/YYYY",
      "Mardi DD/MM/YYYY",
      "Mercredi DD/MM/YYYY",
      "Jeudi DD/MM/YYYY",
      "Vendredi DD/MM/YYYY"
    ],
    "lignes": [
      {
        "heureLabel": "08H-09H",
        "cells": [
          {"type":"empty","rowSpan":1},
          {"type":"course","rowSpan":2,"module":"...","professeur":"...","salle":"..."},
          {"type":"skip"},
          {"type":"empty","rowSpan":1},
          {"type":"empty","rowSpan":1}
        ]
      }
    ],
    "separateurs": [2]
  }
}

Règles "cells":
- Exactement 5 cellules (lundi à vendredi) par ligne.
- type=course => module/professeur/salle obligatoires + rowSpan >= 1.
- type=empty => rowSpan >= 1.
- type=skip => cellule couverte par la fusion verticale d'une ligne précédente.
- Toute fusion verticale doit être cohérente: les lignes suivantes doivent utiliser type=skip.
""".formatted(filiere, niveau, semaineDu, semaineAu, creneauxJson);
    }

    private Map<String,Object> fallbackEdtTemplate(String filiere, String niveau, String semaineDu, String semaineAu, List<Map<String,Object>> creneaux) {
        DateTimeFormatter in = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter out = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate lundi;
        try { lundi = LocalDate.parse(Objects.requireNonNullElse(semaineDu, LocalDate.now().toString()), in); }
        catch (Exception e) { lundi = LocalDate.now(); }

        List<String> colonnes = new ArrayList<>(List.of("Heures"));
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
        for (int i = 0; i < 5; i++) colonnes.add(jours[i] + " " + lundi.plusDays(i).format(out));

        List<Map<String,Object>> lignes = new ArrayList<>();
        String[] slots = {"08H-10H", "10H-12H", "13H-15H", "15H-17H"};
        for (String slot : slots) {
            List<Map<String,Object>> cells = new ArrayList<>();
            for (int i = 0; i < 5; i++) cells.add(Map.of("type", "empty", "rowSpan", 1));
            lignes.add(Map.of("heureLabel", slot, "cells", cells));
        }

        String titre = "EMPLOI DU TEMPS (" + formatJourMois(semaineDu, lundi.toString()) + " - " + formatJourMois(semaineAu, lundi.plusDays(4).toString()) + ")";
        String sousTitre = (Objects.requireNonNullElse(niveau, "") + " : " + Objects.requireNonNullElse(filiere, "")).trim();

        return Map.ofEntries(
            Map.entry("institution", "Institut Supérieur du Digital"),
            Map.entry("titre", titre),
            Map.entry("sousTitre", sousTitre),
            Map.entry("noteBasPage", "NB : L'emploi du temps n'est pas définitif. Il peut toujours subir des modifications."),
            Map.entry("colonnes", colonnes),
            Map.entry("lignes", lignes),
            Map.entry("separateurs", List.of(2))
        );
    }

    private String formatJourMois(String isoDate, String fallbackIsoDate) {
        try {
            LocalDate d = LocalDate.parse(Objects.requireNonNullElse(isoDate, fallbackIsoDate));
            String[] mois = {"JANVIER","FEVRIER","MARS","AVRIL","MAI","JUIN","JUILLET","AOUT","SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE"};
            return d.getDayOfMonth() + " " + mois[d.getMonthValue() - 1] + " " + d.getYear();
        } catch (Exception e) {
            return fallbackIsoDate;
        }
    }
}
