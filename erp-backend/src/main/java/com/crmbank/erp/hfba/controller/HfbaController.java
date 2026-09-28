package com.crmbank.erp.hfba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hfba.mapper.HfbaMapper;
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
 * [HFBA] 원가기준/원가회계 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hfba")
@RequiredArgsConstructor
public class HfbaController {

    private final HfbaMapper hfbaMapper;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. U_STR 및 MOD/DEL 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/FBA1010U_STR")
    public ResponseEntity<?> callFBA1010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA1010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("FBA1010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hfbaMapper.FBA1010U_STR(params);

        if ("S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/FBA1040U_STR")
    public ResponseEntity<?> callFBA1040U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA1040U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("FBA1040U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hfbaMapper.FBA1040U_STR(params);

        if ("S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/FBA1060U_STR")
    public ResponseEntity<?> callFBA1060U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA1060U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("FBA1060U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hfbaMapper.FBA1060U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/FBA2010U_STR")
    public ResponseEntity<?> callFBA2010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA2010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("FBA2010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hfbaMapper.FBA2010U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/FBA2020U_MOD")
    public ResponseEntity<?> callFBA2020U_MOD(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA2020U_MOD", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("FBA2020U_MOD", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hfbaMapper.FBA2020U_MOD(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/FBA2020U_DEL")
    public ResponseEntity<?> callFBA2020U_DEL(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA2020U_DEL", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("FBA2020U_DEL", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hfbaMapper.FBA2020U_DEL(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/FBA3010U_STR")
    public ResponseEntity<?> callFBA3010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA3010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("FBA3010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hfbaMapper.FBA3010U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    // ==========================================
    // 2. SEL 및 직접 조회 쿼리 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/FBA2020U_SEL")
    public ResponseEntity<?> callFBA2020U_SEL(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("FBA2020U_SEL", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("FBA2020U_SEL", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hfbaMapper.FBA2020U_SEL(params)));
    }

    @PostMapping("/SELECT_ACCT_LIST")
    public ResponseEntity<?> selectAcctList(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("🏢 [Exec Query]: selectAcctList");
        return ResponseEntity.ok(convertToLowerCaseKeys(hfbaMapper.selectAcctList(params)));
    }

    @PostMapping("/SELECT_DIVIDE_LIST")
    public ResponseEntity<?> selectDivideList(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("🏢 [Exec Query]: selectDivideList");
        return ResponseEntity.ok(convertToLowerCaseKeys(hfbaMapper.selectDivideList(params)));
    }

    @PostMapping("/SELECT_DIVIDE_JUKSU_LIST")
    public ResponseEntity<?> selectDivideJuksuList(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("🏢 [Exec Query]: selectDivideJuksuList");
        return ResponseEntity.ok(convertToLowerCaseKeys(hfbaMapper.selectDivideJuksuList(params)));
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
            String statementId = HfbaMapper.class.getName() + "." + proc;
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
            String statementId = HfbaMapper.class.getName() + "." + proc;
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
