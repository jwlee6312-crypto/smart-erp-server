package com.crmbank.erp.hafn.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hafn.mapper.HafnMapper;
import com.crmbank.erp.hafn.service.HafnService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * [HAFN] 자금관리 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hafn")
@RequiredArgsConstructor
public class HafnController {

    private final HafnMapper hafnMapper;
    private final HafnService hafnService;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. _SAVE 트랜잭션 서비스 보존
    // ==========================================

    @PostMapping("/HAFN_610U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveHafn610(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hafnService.saveHafn610(payload, user.getCmpycd(), user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hafn] HAFN_610U_SAVE Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HAFN_620U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveHafn620(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hafnService.saveHafn620(payload, user.getCmpycd(), user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hafn] HAFN_620U_SAVE Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HAFN_630U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveHafn630(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hafnService.saveHafn630(payload, user.getCmpycd(), user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hafn] HAFN_630U_SAVE Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HAFN_610U_STR")
    public ResponseEntity<?> callHAFN_610U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_610U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFN_610U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hafnMapper.HAFN_610U_STR(params);

        return ResponseEntity.ok(convertToLowerCaseKeys(raw));
    }

    @PostMapping("/HAFN_620U_STR")
    public ResponseEntity<?> callHAFN_620U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_620U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFN_620U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hafnMapper.HAFN_620U_STR(params);

        return ResponseEntity.ok(convertToLowerCaseKeys(raw));
    }

    @PostMapping("/HAFN_630U_STR")
    public ResponseEntity<?> callHAFN_630U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_630U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFN_630U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hafnMapper.HAFN_630U_STR(params);

        return ResponseEntity.ok(convertToLowerCaseKeys(raw));
    }

    // ==========================================
    // 3. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HAFN_010S_STR")
    public ResponseEntity<?> callHAFN_010S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_010S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_010S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_010S_STR(params)));
    }

    @PostMapping("/HAFN_110S_STR")
    public ResponseEntity<?> callHAFN_110S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_110S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_110S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_110S_STR(params)));
    }

    @PostMapping("/HAFN_120S_STR")
    public ResponseEntity<?> callHAFN_120S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_120S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_120S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_120S_STR(params)));
    }

    @PostMapping("/HAFN_210S_STR")
    public ResponseEntity<?> callHAFN_210S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_210S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_210S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_210S_STR(params)));
    }

    @PostMapping("/HAFN_310S_STR")
    public ResponseEntity<?> callHAFN_310S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_310S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_310S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_310S_STR(params)));
    }

    @PostMapping("/HAFN_410S_STR")
    public ResponseEntity<?> callHAFN_410S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_410S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_410S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_410S_STR(params)));
    }

    @PostMapping("/HAFN_420S_STR")
    public ResponseEntity<?> callHAFN_420S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_420S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_420S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_420S_STR(params)));
    }

    @PostMapping("/HAFN_430S_STR")
    public ResponseEntity<?> callHAFN_430S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_430S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_430S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_430S_STR(params)));
    }

    @PostMapping("/HAFN_510S_STR")
    public ResponseEntity<?> callHAFN_510S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_510S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_510S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_510S_STR(params)));
    }

    @PostMapping("/HAFN_520S_STR")
    public ResponseEntity<?> callHAFN_520S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_520S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_520S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_520S_STR(params)));
    }

    @PostMapping("/HAFN_670S_STR")
    public ResponseEntity<?> callHAFN_670S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_670S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_670S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_670S_STR(params)));
    }

    @PostMapping("/HAFN_680S_STR")
    public ResponseEntity<?> callHAFN_680S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_680S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_680S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_680S_STR(params)));
    }

    @PostMapping("/HAFN_690S_STR")
    public ResponseEntity<?> callHAFN_690S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFN_690S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFN_690S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafnMapper.HAFN_690S_STR(params)));
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

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (params.get("cmpycd") == null || params.get("cmpycd").toString().trim().isEmpty()) {
                params.put("cmpycd", user.getCmpycd());
            }
            if (params.get("userid") == null || params.get("userid").toString().trim().isEmpty()) {
                params.put("userid", user.getUserid());
            }
            params.put("updemp", user.getUserid());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HafnMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);

            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String cleanProp = prop.trim();
                    if (!params.containsKey(cleanProp) || params.get(cleanProp) == null || params.get(cleanProp).toString().trim().isEmpty()) {
                        params.put(cleanProp, "");
                    }
                    if (!cleanProp.equals(prop)) params.put(prop, params.get(cleanProp));
                }
            }
        } catch (Exception e) { log.warn("🛠 missing parameter alarm ({}): {}", proc, e.getMessage()); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HafnMapper.class.getName() + "." + proc;
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
                newMap.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            newList.add(newMap);
        }
        return newList;
    }
}
