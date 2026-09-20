package com.crmbank.erp.comm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/manual/ai")
@RequiredArgsConstructor
public class ManualAiController {

    @Value("${google.ai.gemini.api-key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeManual(@RequestBody Map<String, String> params) {
        log.info("📢 AI 분석 요청 수신: {}", params.get("progid"));
        
        String content = params.get("content");
        String progid = params.get("progid");
        String prognm = params.get("prognm");

        try {
            String prompt = """
                당신은 ERP 전문 컨설턴트입니다. 다음은 [%s] %s 프로그램의 업무 매뉴얼 초안입니다.
                
                ### 원본 내용:
                %s
                
                이 내용을 바탕으로 신입 사원도 이해하기 쉽게 '업무 절차 중심'의 고도화된 매뉴얼을 HTML 형식으로 작성해 주세요.
                조건:
                1. 디자인은 최신 웹 스타일(Bootstrap 기반)을 사용하여 세련되게 만드세요.
                2. 팁과 주의사항 섹션을 반드시 포함하세요.
                3. 답변은 <div>로 시작하는 HTML 코드 본문만 보내주세요.
                """.formatted(progid, prognm, content);

            // 💡 가장 안정적인 gemini-pro 모델을 명시적으로 사용
            String model = "gemini-pro";
            String url = "https://generativelanguage.googleapis.com/v1/models/" + model + ":generateContent?key=" + geminiApiKey;

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("parts", Collections.singletonList(part));
            requestBody.put("contents", Collections.singletonList(contentMap));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("🚀 Gemini API 호출 (Model: gemini-pro)");
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("candidates")) {
                throw new RuntimeException("API 응답 바디가 비어있습니다.");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("생성된 답변(candidates)이 없습니다.");
            }

            Map<String, Object> candidate = candidates.getFirst();
            @SuppressWarnings("unchecked")
            Map<String, Object> candidateContent = (Map<String, Object>) candidate.get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) candidateContent.get("parts");
            Map<String, Object> partMap = parts.getFirst();
            String aiResult = (String) partMap.get("text");

            log.info("✅ AI 분석 성공");
            return ResponseEntity.ok(Map.of("result", aiResult));
            
        } catch (Exception e) {
            log.error("❌ AI 분석 실패: ", e);
            // 만약 gemini-pro도 안될 경우를 대비한 가이드 메시지
            return ResponseEntity.status(500).body("AI 모델 통신 오류. API 키 권한을 확인해 주세요.");
        }
    }
}
