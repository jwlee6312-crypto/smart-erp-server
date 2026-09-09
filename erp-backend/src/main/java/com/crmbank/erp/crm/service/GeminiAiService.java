package com.crmbank.erp.crm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    @Value("${google.ai.gemini.model-name:gemini-1.5-flash}")
    private String modelName;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public Map<String, String> analyze(String chatLog, String audioPath) {
        if (audioPath != null && !audioPath.trim().isEmpty()) return analyzeAudio(audioPath);
        Map<String, String> result = new HashMap<>();
        result.put("stt", chatLog != null ? chatLog : "");
        result.put("summary", summarizeText(chatLog));
        return result;
    }

    public String summarizeText(String chatLog) {
        if (chatLog == null || chatLog.trim().isEmpty()) return "내용 없음";
        try {
            String prompt = "상담 내역을 3줄 요약해줘. JSON 형식 {\"summary\": \"...\"}으로만 응답해:\n\n" + chatLog;
            return parseSimpleField(callGeminiApi(prompt, null), "summary");
        } catch (Exception e) { return "요약 실패"; }
    }

    public Map<String, String> analyzeAudio(String filePath) {
        Map<String, String> result = new HashMap<>();
        try {
            log.info("🎙️ [AI] 분석 파일: {}", filePath);
            java.io.File file = new java.io.File(filePath);
            int retry = 0;
            while ((!file.exists() || file.length() < 100) && retry++ < 5) { Thread.sleep(1000); }

            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String base64Content = Base64.getEncoder().encodeToString(fileContent);
            String prompt = "오디오 상담 분석 후 trb_ment, ans_ment, summary 를 한국어 JSON으로 응답해.";

            Map<String, Object> inlineData = Map.of("mime_type", "audio/wav", "data", base64Content);
            String rawResponse = callGeminiApi(prompt, inlineData);
            
            result.put("trb_ment", parseSimpleField(rawResponse, "trb_ment"));
            result.put("ans_ment", parseSimpleField(rawResponse, "ans_ment"));
            result.put("summary", parseSimpleField(rawResponse, "summary"));
            result.put("stt", result.get("trb_ment") + "\n" + result.get("ans_ment"));
        } catch (Exception e) {
            log.error("🎙️ AI 분석 에러: {}", e.getMessage());
            result.put("summary", "오류 (파일 처리 중)");
        }
        return result;
    }

    private String callGeminiApi(String prompt, Map<String, Object> inlineData) throws Exception {
        String urlStr = "https://generativelanguage.googleapis.com/v1/models/" + modelName.replace("models/", "") + ":generateContent?key=" + apiKey.trim();
        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(
                inlineData != null ? Map.of("text", prompt, "inline_data", inlineData) : Map.of("text", prompt)
        ))));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.postForEntity(URI.create(urlStr), new HttpEntity<>(requestBody, headers), Map.class);
        
        List candidates = (List) response.getBody().get("candidates");
        Map firstCandidate = (Map) candidates.get(0);
        Map content = (Map) firstCandidate.get("content");
        List parts = (List) content.get("parts");
        return (String) ((Map) parts.get(0)).get("text");
    }

    private String parseSimpleField(String rawText, String field) {
        try {
            int start = rawText.indexOf("{"); int end = rawText.lastIndexOf("}");
            if (start != -1 && end != -1) {
                Map map = objectMapper.readValue(rawText.substring(start, end + 1), Map.class);
                return String.valueOf(map.getOrDefault(field, rawText));
            }
        } catch (Exception e) {}
        return rawText;
    }
}
