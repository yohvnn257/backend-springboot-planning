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

    /**
     * Donne des CONSEILS textuels sur l'EDT (pas de modification automatique).
     * Retourne : { suggestions: "...", iaActive: bool, edtTemplate: {...} }
     * Le edtTemplate est genere localement (deterministe) pour le rendu HTML email.
     */
    public Map<String,Object> optimiserEdt(String filiere, String niveau, String semaineDu, String semaineAu, List<Map<String,Object>> creneaux) {
        Map<String,Object> base = new LinkedHashMap<>();
        base.put("edtTemplate", fallbackEdtTemplate(filiere, niveau, semaineDu, semaineAu, creneaux));
        base.put("creneauxOptimises", creneaux);

        if (apiKey == null || apiKey.isBlank()) {
            base.put("suggestions", "IA non configuree — ajoutez GEMINI_API_KEY dans Render.");
            base.put("iaActive", false);
            return base;
        }
        try {
            String creneauxJson = objectMapper.writeValueAsString(creneaux);
            String prompt = buildConseilsPrompt(filiere, niveau, semaineDu, semaineAu, creneauxJson);
            Map<String,Object> response = appelerGemini(prompt, 4096, "conseilsEdt");
            String suggestions = (String) response.getOrDefault("suggestions", "");
            base.put("suggestions", suggestions.isBlank() ? "L'IA n'a pas retourne de conseil." : suggestions);
            base.put("iaActive", true);
            return base;
        } catch (Exception e) {
            log.error("Erreur Gemini API (optimiserEdt): {}", e.getMessage());
            base.put("suggestions", "Erreur IA: " + e.getMessage());
            base.put("iaActive", false);
            return base;
        }
    }

    /** Genere un message d'introduction personnalise pour l'email EDT envoye aux etudiants. */
    public String genererIntroEmail(String filiere, String niveau, String semaineDu, String semaineAu, List<Map<String,Object>> creneaux) {
        String fallback = "Voici votre emploi du temps pour la semaine du " + semaineDu + " au " + semaineAu + ". Bonne semaine !";
        if (apiKey == null || apiKey.isBlank()) return fallback;
        try {
            String creneauxJson = objectMapper.writeValueAsString(creneaux);
            String prompt = buildIntroPrompt(filiere, niveau, semaineDu, semaineAu, creneauxJson);
            Map<String,Object> response = appelerGemini(prompt, 1024, "introEmail");
            String texte = (String) response.getOrDefault("intro", "");
            return texte.isBlank() ? fallback : texte;
        } catch (Exception e) {
            log.warn("Gemini intro email indisponible : {}", e.getMessage());
            return fallback;
        }
    }

    /** Analyse les disponibilites soumises par les profs. */
    public Map<String,Object> analyserDisponibilites(List<Map<String,Object>> dispos) {
        Map<String,Object> base = new LinkedHashMap<>();
        if (apiKey == null || apiKey.isBlank()) {
            base.put("analyse", "IA non configuree — ajoutez GEMINI_API_KEY dans Render.");
            base.put("iaActive", false);
            return base;
        }
        if (dispos == null || dispos.isEmpty()) {
            base.put("analyse", "Aucune disponibilite a analyser.");
            base.put("iaActive", true);
            return base;
        }
        try {
            String json = objectMapper.writeValueAsString(dispos);
            String prompt = buildAnalysePrompt(json);
            Map<String,Object> response = appelerGemini(prompt, 4096, "analyserDispos");
            String analyse = (String) response.getOrDefault("analyse", "");
            base.put("analyse", analyse.isBlank() ? "L'IA n'a pas retourne d'analyse." : analyse);
            base.put("nombreCreneaux", response.getOrDefault("nombreCreneaux", dispos.size()));
            base.put("recommandations", response.getOrDefault("recommandations", ""));
            base.put("iaActive", true);
            return base;
        } catch (Exception e) {
            log.error("Erreur Gemini API (analyserDispos): {}", e.getMessage());
            base.put("analyse", "Erreur IA: " + e.getMessage());
            base.put("iaActive", false);
            return base;
        }
    }

    /** Appel HTTP générique vers l'API Gemini avec fallback automatique sur gemini-2.5-flash si 503/UNAVAILABLE. */
    @SuppressWarnings({"unchecked","rawtypes"})
    private Map<String,Object> appelerGemini(String prompt, int maxTokens, String contexte) throws Exception {
        // thinkingBudget=0 = desactive le mode "thinking" de Gemini 2.5 Flash qui consomme la moitie du budget tokens.
        // Sans ca, Gemini 2.5 Flash "reflechit" en interne et tronque sa reponse JSON (finishReason=MAX_TOKENS).
        Map<String,Object> body = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.ofEntries(
                Map.entry("responseMimeType", "application/json"),
                Map.entry("maxOutputTokens", maxTokens),
                Map.entry("temperature", 0.3),
                Map.entry("thinkingConfig", Map.of("thinkingBudget", 0))
            )
        );
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-goog-api-key", apiKey);

        ResponseEntity<Map> resp;
        try {
            String url = apiUrl.replace("{model}", model);
            resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
        } catch (org.springframework.web.client.HttpServerErrorException | org.springframework.web.client.HttpClientErrorException ex) {
            // Fallback automatique : si modele preview surcharge (503) ou rate-limited (429), on bascule sur gemini-2.5-flash stable
            int code = ex.getStatusCode().value();
            String FALLBACK = "gemini-2.5-flash";
            if ((code == 503 || code == 429 || code >= 500) && !FALLBACK.equals(model)) {
                log.warn("Gemini {} indisponible (HTTP {}). Fallback automatique sur {}", model, code, FALLBACK);
                String fallbackUrl = apiUrl.replace("{model}", FALLBACK);
                resp = restTemplate.exchange(fallbackUrl, HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            } else {
                throw ex;
            }
        }
        List<Map<String,Object>> candidates = (List<Map<String,Object>>) resp.getBody().get("candidates");
        if (candidates == null || candidates.isEmpty())
            throw new RuntimeException("Réponse Gemini vide");
        // Si Gemini a coupe la reponse, finishReason = "MAX_TOKENS" -> on logge
        Object finishReason = candidates.get(0).get("finishReason");
        Map<String,Object> content = (Map<String,Object>) candidates.get(0).get("content");
        if (content == null) throw new RuntimeException("Réponse Gemini sans content (finishReason=" + finishReason + ")");
        List<Map<String,Object>> parts = (List<Map<String,Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) throw new RuntimeException("Réponse Gemini sans parts");
        String texte = (String) parts.get(0).get("text");
        if (texte == null || texte.isBlank()) throw new RuntimeException("Texte Gemini vide");

        // Nettoyage : Gemini peut entourer son JSON de ```json ... ``` malgre responseMimeType
        String json = texte.trim();
        if (json.startsWith("```")) {
            int firstNl = json.indexOf('\n');
            if (firstNl > 0) json = json.substring(firstNl + 1);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            json = json.trim();
        }

        try {
            Map<String,Object> result = objectMapper.readValue(json, Map.class);
            result.put("iaActive", true);
            log.debug("Gemini {} OK ({} chars, finishReason={})", contexte, json.length(), finishReason);
            return result;
        } catch (Exception parseError) {
            log.error("Gemini {} JSON invalide (finishReason={}, longueur={}). Debut: {}", contexte, finishReason, json.length(), json.substring(0, Math.min(200, json.length())));
            // Retry auto si MAX_TOKENS et qu'on n'est pas deja a la limite (16384 = budget large pour 2.5 Flash)
            if ("MAX_TOKENS".equals(String.valueOf(finishReason)) && maxTokens < 16384) {
                int newMax = Math.min(maxTokens * 2, 16384);
                log.warn("Gemini {} retry avec maxTokens={} (etait {})", contexte, newMax, maxTokens);
                return appelerGemini(prompt, newMax, contexte + "-retry");
            }
            throw new RuntimeException("JSON Gemini invalide ou tronque (finishReason=" + finishReason + ")");
        }
    }

    private String buildConseilsPrompt(String filiere, String niveau, String semaineDu, String semaineAu, String creneauxJson) {
        return """
EDT %s %s du %s au %s, creneaux : %s

Donne 2-3 conseils concrets (max 200 chars total) sur equilibrage jours, charge matin/apm, doubles bookings, jours vides.
Tutoie la secretaire.

JSON pur : {"suggestions":"conseil1\\nconseil2"}
""".formatted(filiere, niveau, semaineDu, semaineAu, creneauxJson);
    }

    private String buildIntroPrompt(String filiere, String niveau, String semaineDu, String semaineAu, String creneauxJson) {
        return """
Intro mail EDT %s %s, semaine %s au %s, cours : %s

Redige 1 paragraphe court (2 phrases max, 150 chars). Vouvoiement. Mentionne nombre cours + jours.
Pas de "Bonjour" ni "Cordialement".

JSON pur : {"intro":"texte"}
""".formatted(filiere, niveau, semaineDu, semaineAu, creneauxJson);
    }

    private String buildAnalysePrompt(String dispos) {
        return """
Analyse les disponibilites profs (JSON) : %s

Reponds en JSON pur :
{"analyse":"3 phrases max sur la couverture jours/horaires","recommandations":"2 actions courtes separees par \\n"}
""".formatted(dispos);
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

        String titre = "EMPLOI DU TEMPS (" + formatPeriode(semaineDu, semaineAu, lundi) + ")";
        String sousTitre = (formatNiveau(niveau) + " : " + Objects.requireNonNullElse(filiere, "").toUpperCase()).trim();

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

    /** Format conforme maquette ISD : "27 - 30 AVRIL 2026" si meme mois, sinon "29 AVRIL - 3 MAI 2026". */
    private String formatPeriode(String du, String au, LocalDate fallback) {
        String[] mois = {"JANVIER","FEVRIER","MARS","AVRIL","MAI","JUIN","JUILLET","AOUT","SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE"};
        try {
            LocalDate d1 = LocalDate.parse(Objects.requireNonNullElse(du, fallback.toString()));
            LocalDate d2 = LocalDate.parse(Objects.requireNonNullElse(au, fallback.plusDays(4).toString()));
            if (d1.getMonth() == d2.getMonth() && d1.getYear() == d2.getYear())
                return d1.getDayOfMonth() + " - " + d2.getDayOfMonth() + " " + mois[d1.getMonthValue() - 1] + " " + d1.getYear();
            if (d1.getYear() == d2.getYear())
                return d1.getDayOfMonth() + " " + mois[d1.getMonthValue() - 1] + " - " + d2.getDayOfMonth() + " " + mois[d2.getMonthValue() - 1] + " " + d1.getYear();
            return d1.getDayOfMonth() + " " + mois[d1.getMonthValue() - 1] + " " + d1.getYear() + " - " + d2.getDayOfMonth() + " " + mois[d2.getMonthValue() - 1] + " " + d2.getYear();
        } catch (Exception e) {
            return formatJourMois(du, fallback.toString()) + " - " + formatJourMois(au, fallback.plusDays(4).toString());
        }
    }

    /** Format niveau : L3 -> LICENCE 3, M2 -> MASTER 2. */
    private String formatNiveau(String niveau) {
        if (niveau == null || niveau.isBlank()) return "";
        String code = niveau.trim().toUpperCase();
        if (code.startsWith("L")) return "LICENCE " + code.substring(1);
        if (code.startsWith("M")) return "MASTER " + code.substring(1);
        return code;
    }
}
