package com.crmbank.erp.hacl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hacl.mapper.HaclMapper;
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
 * [HACL] 회계결산/재무제표 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hacl")
@RequiredArgsConstructor
public class HaclController {

    private final HaclMapper haclMapper;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HACL_800U_STR")
    public ResponseEntity<?> callHACL_800U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_800U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HACL_800U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haclMapper.HACL_800U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "cmpycd", "yy");
        String code = String.valueOf(resultRow.get("cmpycd")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("yy")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HACL_900U_STR")
    public ResponseEntity<?> callHACL_900U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_900U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HACL_900U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haclMapper.HACL_900U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "cmpycd", "clsyy");
        String code = String.valueOf(resultRow.get("cmpycd")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("clsyy")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    // ==========================================
    // 2. S_STR 현황 및 재무제표 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HACL_010S_STR")
    public ResponseEntity<?> callHACL_010S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_010S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_010S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_010S_STR(params)));
    }

    @PostMapping("/HACL_020S_STR")
    public ResponseEntity<?> callHACL_020S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_020S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_020S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_020S_STR(params)));
    }

    @PostMapping("/HACL_030S_STR")
    public ResponseEntity<?> callHACL_030S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_030S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_030S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_030S_STR(params)));
    }

    @PostMapping("/HACL_040S_STR")
    public ResponseEntity<?> callHACL_040S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_040S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_040S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_040S_STR(params)));
    }

    @PostMapping("/HACL_050S_STR")
    public ResponseEntity<?> callHACL_050S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_050S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_050S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_050S_STR(params)));
    }

    @PostMapping("/HACL_060S_STR")
    public ResponseEntity<?> callHACL_060S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_060S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_060S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_060S_STR(params)));
    }

    @PostMapping("/HACL_070S_STR")
    public ResponseEntity<?> callHACL_070S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_070S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_070S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_070S_STR(params)));
    }

    @PostMapping("/HACL_080S_STR")
    public ResponseEntity<?> callHACL_080S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HACL_080S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HACL_080S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haclMapper.HACL_080S_STR(params)));
    }

    // ==========================================
    // 3. 공통 유틸리티 헬퍼 메서드
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
            String statementId = HaclMapper.class.getName() + "." + proc;
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
            String statementId = HaclMapper.class.getName() + "." + proc;
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
