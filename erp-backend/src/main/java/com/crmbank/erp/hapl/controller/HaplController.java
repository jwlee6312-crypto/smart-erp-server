package com.crmbank.erp.hapl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hapl.mapper.HaplMapper;
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
 * [HAPL] 관리손익 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hapl")
@RequiredArgsConstructor
public class HaplController {

    private final HaplMapper haplMapper;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HAPL_010U_STR")
    public ResponseEntity<?> callHAPL_010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAPL_010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haplMapper.HAPL_010U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAPL_020U_STR")
    public ResponseEntity<?> callHAPL_020U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_020U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAPL_020U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haplMapper.HAPL_020U_STR(params);

        if ("S0".equals(actkind) || "M0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAPL_030U_STR")
    public ResponseEntity<?> callHAPL_030U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_030U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAPL_030U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haplMapper.HAPL_030U_STR(params);

        if ("S2".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind)  || "DR".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAPL_040U_STR")
    public ResponseEntity<?> callHAPL_040U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_040U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAPL_040U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haplMapper.HAPL_040U_STR(params);

        if ("M0".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAPL_050U_STR")
    public ResponseEntity<?> callHAPL_050U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_050U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAPL_050U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = haplMapper.HAPL_050U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind)  || "DR".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAPL_100U_STR")
    public ResponseEntity<?> callHAPL_100U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_100U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAPL_100U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "C")).toUpperCase();
        List<Map<String, Object>> raw = haplMapper.HAPL_100U_STR(params);

        return ResponseEntity.ok(convertToLowerCaseKeys(raw));
    }

    @PostMapping("/HAPL_200U_STR")
    public ResponseEntity<?> callHAPL_200U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_200U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAPL_200U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "C")).toUpperCase();
        List<Map<String, Object>> raw = haplMapper.HAPL_200U_STR(params);

        return ResponseEntity.ok(convertToLowerCaseKeys(raw));
    }

    // ==========================================
    // 2. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HAPL_110S_STR")
    public ResponseEntity<?> callHAPL_110S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_110S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAPL_110S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haplMapper.HAPL_110S_STR(params)));
    }

    @PostMapping("/HAPL_110S_INIT")
    public ResponseEntity<?> callHAPL_110S_INIT(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_110S_INIT", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAPL_110S_INIT", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haplMapper.HAPL_110S_INIT(params)));
    }

    @PostMapping("/HAPL_120S_STR")
    public ResponseEntity<?> callHAPL_120S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_120S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAPL_120S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haplMapper.HAPL_120S_STR(params)));
    }

    @PostMapping("/HAPL_140S_STR")
    public ResponseEntity<?> callHAPL_140S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_140S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAPL_140S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haplMapper.HAPL_140S_STR(params)));
    }

    @PostMapping("/HAPL_210S_STR")
    public ResponseEntity<?> callHAPL_210S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_210S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAPL_210S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haplMapper.HAPL_210S_STR(params)));
    }

    @PostMapping("/HAPL_220S_STR")
    public ResponseEntity<?> callHAPL_220S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAPL_220S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAPL_220S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(haplMapper.HAPL_220S_STR(params)));
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
            String statementId = HaplMapper.class.getName() + "." + proc;
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
            String statementId = HaplMapper.class.getName() + "." + proc;
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
