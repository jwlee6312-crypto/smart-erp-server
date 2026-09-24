package com.crmbank.erp.hasl.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hasl.mapper.HaslMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * [HASL] 전표/회계관리 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hasl")
@RequiredArgsConstructor
public class HaslController {

    private final HaslMapper haslMapper;
    private final SqlSession sqlSession;
    private final ObjectMapper objectMapper;

    // ==========================================
    // 1. _SAVE 트랜잭션 서비스
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/HASL_010U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveSlip010(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            log.info("📥 [HASL_010U_SAVE] 요청 수신. Payload: {}", payload);
            Map<String, Object> master = objectMapper.convertValue(payload.get("master"), new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> details = objectMapper.convertValue(payload.get("details"), new TypeReference<List<Map<String, Object>>>() {});
            String actkind = String.valueOf(payload.getOrDefault("actkind", "A")).toUpperCase();

            injectSession(master, session);
            master.put("actkind", actkind);
            
            fillMissingParameters("HASL_010U_STR", master);
            log.info("🏢 [Master Exec SQL]: {}", buildPositionalSql("HASL_010U_STR", master));
            List<Map<String, Object>> masterresultRaw = haslMapper.HASL_010U_STR(master);

            if (masterresultRaw == null || masterresultRaw.isEmpty()) {
                if ("D".equals(actkind)) return ResponseEntity.ok(ApiResponse.success(null, "삭제되었습니다."));
                throw new RuntimeException("마스터 처리 실패 (결과 없음)");
            }
            
            List<Map<String, Object>> masterresult = convertToLowerCaseKeys(masterresultRaw);
            Map<String, Object> firstRow = masterresult.get(0);
            log.info("📊 [Master Result]: {}", firstRow);

            String firstVal = extractFirstValue(masterresult);
            if ("D".equals(actkind)) {
                if (firstVal.equals(master.get("slipno"))) {
                    log.warn("⚠️ [Delete Warning] 삭제 요청했으나 DB에서 기존 번호 반환. 삭제 스킵되었을 수 있음.");
                }
            }

            if ("00".equals(String.valueOf(firstRow.getOrDefault("result", ""))) || "000000".equals(firstVal)) {
                throw new RuntimeException(String.valueOf(firstRow.getOrDefault("msg", "마스터 처리 오류")));
            }

            String slipno = firstVal;
            if (details == null || details.isEmpty()) {
                log.info("ℹ️ [Details] 처리할 상세 내역 데이터가 없습니다.");
            } else if (!"D".equals(actkind)) {
                log.info("📝 [Details] 상세 행 {}건 처리 시작", details.size());
                for (int i = 0; i < details.size(); i++) {
                    Map<String, Object> detail = details.get(i);
                    injectSession(detail, session);
                    detail.put("slipymd", master.get("slipymd"));
                    detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", String.valueOf(detail.getOrDefault("upkind", actkind)).toUpperCase());
                    
                    fillMissingParameters("HASL_011U_STR", detail);
                    log.info("📑 [Detail Exec SQL]: {}", buildPositionalSql("HASL_011U_STR", detail));
                    List<Map<String, Object>> dtlResRaw = haslMapper.HASL_011U_STR(detail);
                    
                    if (dtlResRaw != null && !dtlResRaw.isEmpty()) {
                        Map<String, Object> resRow = convertToLowerCaseKeys(dtlResRaw).get(0);
                        if ("00".equals(String.valueOf(resRow.getOrDefault("result", "")))) {
                            throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "상세 처리 실패"));
                        }
                    }
                }
            }
            return ResponseEntity.ok(ApiResponse.success(Map.of("slipno", slipno), "성공적으로 처리되었습니다."));
        } catch (Exception e) {
            log.error("❌ [saveSlip010] Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/HASL_110U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveSlip110(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            log.info("📥 [HASL_110U_SAVE] 요청 수신. Payload: {}", payload);
            Map<String, Object> master = objectMapper.convertValue(payload.get("master"), new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> details = objectMapper.convertValue(payload.get("details"), new TypeReference<List<Map<String, Object>>>() {});
            String actkind = String.valueOf(payload.getOrDefault("actkind", "A")).toUpperCase();

            injectSession(master, session);
            master.put("actkind", actkind);
            
            fillMissingParameters("HASL_110U_STR", master);
            log.info("🏢 [Master Exec SQL]: {}", buildPositionalSql("HASL_110U_STR", master));
            List<Map<String, Object>> masterresultRaw = haslMapper.HASL_110U_STR(master);

            if (masterresultRaw == null || masterresultRaw.isEmpty()) {
                if ("D".equals(actkind)) return ResponseEntity.ok(ApiResponse.success(null, "삭제되었습니다."));
                throw new RuntimeException("마스터 처리 실패 (결과 없음)");
            }
            
            List<Map<String, Object>> masterresult = convertToLowerCaseKeys(masterresultRaw);
            Map<String, Object> firstRow = masterresult.get(0);
            log.info("📊 [Master Result]: {}", firstRow);

            String resCode = String.valueOf(firstRow.getOrDefault("result", "")).trim();
            String firstVal = extractFirstValue(masterresult);
            if ("00".equals(resCode) || "00".equals(firstVal) || "000000".equals(firstVal)) {
                throw new RuntimeException(String.valueOf(firstRow.getOrDefault("msg", "마스터 처리 오류")));
            }

            String slipno = firstVal;
            if (details != null && !"D".equals(actkind)) {
                for (int i = 0; i < details.size(); i++) {
                    Map<String, Object> detail = details.get(i);
                    injectSession(detail, session);
                    detail.put("slipymd", master.get("slipymd")); 
                    detail.put("slipno", slipno);
                    detail.put("acctymd", master.get("acctymd"));
                    detail.put("actkind", String.valueOf(detail.getOrDefault("upkind", actkind)).toUpperCase());
                    
                    fillMissingParameters("HASL_111U_STR", detail);
                    log.info("📑 [Detail Exec SQL]: {}", buildPositionalSql("HASL_111U_STR", detail));
                    List<Map<String, Object>> dtlResRaw = haslMapper.HASL_111U_STR(detail);
                    
                    if (dtlResRaw != null && !dtlResRaw.isEmpty()) {
                        Map<String, Object> resRow = convertToLowerCaseKeys(dtlResRaw).get(0);
                        if ("00".equals(String.valueOf(resRow.getOrDefault("result", "")))) {
                            throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "상세 처리 실패"));
                        }
                    }
                }
            }
            return ResponseEntity.ok(ApiResponse.success(Map.of("slipno", slipno), "성공적으로 처리되었습니다."));
        } catch (Exception e) {
            log.error("❌ [saveSlip110] Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HASL_010U_STR")
    public ResponseEntity<?> callHASL_010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HASL_010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haslMapper.HASL_010U_STR(params);

        if ("S".equals(actkind) || "P".equals(actkind) || "N".equals(actkind) || "F".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("00000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HASL_011U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHASL_011U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HASL_011U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_011U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HASL_011U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HASL_011U_STR", detail));

            List<Map<String, Object>> raw = haslMapper.HASL_011U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "msg")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HASL_020U_STR")
    public ResponseEntity<?> callHASL_020U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_020U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HASL_020U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haslMapper.HASL_020U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HASL_110U_STR")
    public ResponseEntity<?> callHASL_110U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_110U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HASL_110U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = haslMapper.HASL_110U_STR(params);

        if ("S".equals(actkind) || "P".equals(actkind) || "N".equals(actkind) || "F".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("00000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HASL_111U_STR")
    public ResponseEntity<?> callHASL_111U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HASL_111U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_111U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HASL_111U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HASL_111U_STR", detail));

            List<Map<String, Object>> raw = haslMapper.HASL_111U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "msg")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    // ==========================================
    // 3. S_STR 및 조회/마스터 전용 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HASL_030S_STR")
    public ResponseEntity<?> callHASL_030S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_030S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_030S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_030S_STR(params)));
    }

    @PostMapping("/HASL_040S_STR")
    public ResponseEntity<?> callHASL_040S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_040S_STR", params);
        String rawkeyword = String.valueOf(params.getOrDefault("keyword", "")).trim();
        if (!rawkeyword.isEmpty() && !rawkeyword.equalsIgnoreCase("null")) {
            params.put("keywords", modernTokenize(rawkeyword));
        }
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_040S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_040S_STR(params)));
    }

    @PostMapping("/HASL_050U_MASTER")
    public ResponseEntity<?> callHASL_050U_MASTER(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_050U_MASTER", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_050U_MASTER", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_050U_MASTER(params)));
    }

    @PostMapping("/HASL_050U_STR")
    public ResponseEntity<?> callHASL_050U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_050U_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_050U_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_050U_STR(params)));
    }

    @PostMapping("/HASL_120S_STR")
    public ResponseEntity<?> callHASL_120S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_120S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_120S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_120S_STR(params)));
    }

    @PostMapping("/HASL_130S_STR")
    public ResponseEntity<?> callHASL_130S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_130S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_130S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_130S_STR(params)));
    }

    @PostMapping("/HASL_510S_STR")
    public ResponseEntity<?> callHASL_510S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_510S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_510S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_510S_STR(params)));
    }

    @PostMapping("/HASL_520S_STR")
    public ResponseEntity<?> callHASL_520S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_520S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_520S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_520S_STR(params)));
    }

    @PostMapping("/HASL_530S_STR")
    public ResponseEntity<?> callHASL_530S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_530S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_530S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_530S_STR(params)));
    }

    @PostMapping("/HASL_540S_STR")
    public ResponseEntity<?> callHASL_540S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_540S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_540S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_540S_STR(params)));
    }

    @PostMapping("/HASL_550S_STR")
    public ResponseEntity<?> callHASL_550S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_550S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_550S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_550S_STR(params)));
    }

    @PostMapping("/HASL_560S_STR")
    public ResponseEntity<?> callHASL_560S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_560S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_560S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_560S_STR(params)));
    }

    @PostMapping("/HASL_610S_STR")
    public ResponseEntity<?> callHASL_610S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_610S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_610S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_610S_STR(params)));
    }

    @PostMapping("/HASL_620S_STR")
    public ResponseEntity<?> callHASL_620S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_620S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_620S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_620S_STR(params)));
    }

    @PostMapping("/HASL_630S_STR")
    public ResponseEntity<?> callHASL_630S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_630S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_630S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_630S_STR(params)));
    }

    @PostMapping("/HASL_710S_STR")
    public ResponseEntity<?> callHASL_710S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HASL_710S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HASL_710S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haslMapper.HASL_710S_STR(params)));
    }

    // ==========================================
    // 4. 공통 유틸리티 헬퍼 메서드
    // ==========================================

    private Map<String, Object> mapToAlias(Map<String, Object> rawRow, String col1Alias, String col2Alias) {
        Map<String, Object> newMap = new LinkedHashMap<>();
        int i = 1;
        for (Map.Entry<String, Object> entry : rawRow.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.startsWith("col") || key.isEmpty()) {
                if (i == 1) key = col1Alias;
                else if (i == 2) key = col2Alias;
            }
            newMap.put(key, entry.getValue() == null ? "" : entry.getValue());
            i++;
        }
        return newMap;
    }

    private String extractFirstValue(List<Map<String, Object>> result) {
        if (result == null || result.isEmpty()) return "";
        Map<String, Object> firstRow = result.get(0);
        if (firstRow == null || firstRow.isEmpty()) return "";
        return String.valueOf(firstRow.values().iterator().next()).trim();
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd().trim());
            params.putIfAbsent("userid", user.getUserid().trim());
            params.put("updemp", user.getUserid().trim());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HaslMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String key = prop.trim();
                    if (params.get(key) == null || String.valueOf(params.get(key)).trim().isEmpty()) {
                        params.put(key, "");
                    }
                }
            }
        } catch (Exception e) { log.warn("🛠 파라미터 보정 중: {}", e.getMessage()); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HaslMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return "EXEC " + proc;
            BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(statementId).getBoundSql(params);
            List<String> values = new ArrayList<>();
            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                Object val = params.get(pm.getProperty().trim());
                String valStr = (val == null || "null".equals(String.valueOf(val))) ? "''" : "N'" + val.toString().replace("'", "''").trim() + "'";
                values.add(valStr);
            }
            return String.format("EXEC %s %s", proc, String.join(", ", values));
        } catch (Exception e) { return "EXEC " + proc; }
    }

    private List<Map<String, Object>> convertToLowerCaseKeys(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        List<Map<String, Object>> newList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                newMap.put(entry.getKey().toLowerCase(), entry.getValue() == null ? "" : entry.getValue());
            }
            newList.add(newMap);
        }
        return newList;
    }

    private List<String> modernTokenize(String query) {
        if (query == null || query.trim().isEmpty()) return new ArrayList<>();
        String cleaned = query.replaceAll("[.,?!()\\[\\]]", " ").replaceAll("\\d", "").replace("원", "").replaceAll("\\s+", " ").trim();
        String particleregex = "(은|는|이|가|을|를|과|와|로|으로|에서|에게|의|도|만|까지|부터|하고|했다|하며|하였다|했으며|하였습니다|하였으며|이다|입니다|습니까|니까|함|했고|있다|았다|었다|들과|들의|같이)$";
        Pattern pattern = Pattern.compile(particleregex);
        String[] words = cleaned.split(" ");
        List<String> result = new ArrayList<>();
        for (String word : words) {
            String prev = ""; String curr = word;
            while(!prev.equals(curr)) { prev = curr; curr = pattern.matcher(curr).replaceAll(""); }
            if (!curr.isEmpty() && !result.contains(curr)) result.add(curr);
        }
        return result;
    }
}
