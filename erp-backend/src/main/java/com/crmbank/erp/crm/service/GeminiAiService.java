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

    /**
     * 🤖 [복구] 통합 상담 분석 (텍스트 또는 오디오)
     * ConsultSaveService와 OutboundService에서 참조하는 핵심 메서드
     */
    public Map<String, String> analyze(String chatLog, String audioPath) {
        try {
            if (audioPath != null && !audioPath.trim().isEmpty()) {
                return analyzeAudio(audioPath);
            } else if (chatLog != null && !chatLog.trim().isEmpty()) {
                Map<String, String> result = new HashMap<>();
                result.put("stt", chatLog);
                result.put("summary", summarizeText(chatLog));
                return result;
            }
        } catch (Exception e) {
            log.error("AI Analysis Error: {}", e.getMessage());
        }
        return Map.of("stt", chatLog != null ? chatLog : "", "summary", "분석 실패");
    }

    /**
     * 💬 텍스트 요약
     */
    public String summarizeText(String chatLog) {
        if (chatLog == null || chatLog.trim().isEmpty()) return "요약할 내용 없음";
        try {
            String prompt = "다음 상담 내역을 한국어로 3줄 요약해줘. JSON 형식 {\"summary\": \"...\"}으로만 응답해:\n\n" + chatLog;
            return parseSimpleField(callGeminiApi(prompt, null), "summary");
        } catch (Exception e) {
            return "요약 실패 (" + e.getMessage() + ")";
        }
    }

    /**
     * 🎙️ 오디오 분석 (WAV 전용)
     */
    public Map<String, String> analyzeAudio(String filePath) {
        Map<String, String> result = new HashMap<>();
        try {
            log.info("🎙️ [AI] 파일 분석 시작: {}", filePath);
            java.io.File file = new java.io.File(filePath);
            
            // 파일이 안정적으로 생성될 때까지 대기
            int retry = 0;
            while (!file.exists() || file.length() < 100) {
                if (retry++ > 5) break;
                Thread.sleep(1000);
            }

            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String base64Content = Base64.getEncoder().encodeToString(fileContent);
            
            String prompt = "이 오디오 상담 내용을 분석하여 다음 3가지를 한국어로 작성해줘.\n" +
                            "1. 고객의 요청 및 문제점 (trb_ment)\n" +
                            "2. 상담원의 응대 및 답변 (ans_ment)\n" +
                            "3. 전체 상담 3줄 요약 (summary)\n\n" +
                            "JSON 형식으로 응답: {\"trb_ment\": \"...\", \"ans_ment\": \"...\", \"summary\": \"...\"}";

            Map<String, Object> inlineData = Map.of("mime_type", "audio/wav", "data", base64Content);
            String rawResponse = callGeminiApi(prompt, inlineData);
            
            result.put("trb_ment", parseSimpleField(rawResponse, "trb_ment"));
            result.put("ans_ment", parseSimpleField(rawResponse, "ans_ment"));
            result.put("summary", parseSimpleField(rawResponse, "summary"));
            result.put("stt", result.get("trb_ment") + "\n\n" + result.get("ans_ment"));
            
        } catch (Exception e) {
            log.error("🎙️ AI 분석 실패: {}", e.getMessage());
            result.put("summary", "오류 (" + e.getMessage() + ")");
        }
        return result;
    }

    private String callGeminiApi(String prompt, Map<String, Object> inlineData) throws Exception {
        String urlStr = "https://generativelanguage.googleapis.com/v1/models/" + modelName.replace("models/", "") + ":generateContent?key=" + apiKey.trim();
        URI uri = URI.create(urlStr);
        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(
                inlineData != null ? Map.of("text", prompt, "inline_data", inlineData) : Map.of("text", prompt)
        ))));
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.postForEntity(uri, new HttpEntity<>(requestBody, headers), Map.class);
        
        List<Map> candidates = (List<Map>) response.getBody().get("candidates");
        if (candidates == null || candidates.isEmpty()) throw new RuntimeException("API 응답 없음");
        Map content = (Map) candidates.get(0).get("content");
        List<Map> responseParts = (List<Map>) content.get("parts");
        return (String) responseParts.get(0).get("text");
    }

    private String parseSimpleField(String rawText, String field) {
        try {
            int start = rawText.indexOf("{");
            int end = rawText.lastIndexOf("}");
            if (start != -1 && end != -1) {
                Map<String, Object> map = objectMapper.readValue(rawText.substring(start, end + 1), new TypeReference<>() {});
                return String.valueOf(map.getOrDefault(field, rawText));
            }
            return rawText;
        } catch (Exception e) { return rawText; }
    }
}
