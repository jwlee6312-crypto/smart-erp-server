package com.crmbank.erp.hscl.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hscl.mapper.HsclMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * [HSCL] 영업/수불 결산관리 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hscl")
@RequiredArgsConstructor
public class HsclController {

    private final HsclMapper hsclMapper;
    private final SqlSession sqlSession;
    private final JdbcTemplate jdbcTemplate;

    // ==========================================
    // 1. _SAVE 특별 전표 처리 엔드포인트
    // ==========================================

    @PostMapping("/HSCL_110U_SAVE")
    public ResponseEntity<?> saveHscl110(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        try {
            String sql = buildPositionalSql("HSCL_110U_STR", params);
            List<Map<String, Object>> res = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("res", rs.getString(1));
                row.put("msg", rs.getString(2));
                return row;
            });
            return ResponseEntity.ok(convertToLowerCaseKeys(res));
        } catch (Exception e) {
            log.error("❌ [hscl] HSCL_110U_SAVE Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/HSCL_115U_SAVE")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> saveHscl115(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        try {
            List<Map<String, Object>> list = (List<Map<String, Object>>) params.get("list");
            String actkind = (String) params.get("actkind");
            if (list != null) {
                for (Map<String, Object> item : list) {
                    injectSession(item, session);
                    item.put("actkind", actkind);
                    String sql = buildPositionalSql("HSCL_115U_STR", item);
                    jdbcTemplate.execute(sql);
                }
            }
            return ResponseEntity.ok(List.of(Map.of("res", "OK", "result", "ok")));
        } catch (Exception e) {
            log.error("❌ [hscl] HSCL_115U_SAVE Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HSCL_100U_STR")
    public ResponseEntity<?> callHSCL_100U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_100U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSCL_100U_STR", params));

        List<Map<String, Object>> raw = hsclMapper.HSCL_100U_STR(params);
        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSCL_110U_STR")
    public ResponseEntity<?> callHSCL_110U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_110U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSCL_110U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsclMapper.HSCL_110U_STR(params);

        if ("S".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSCL_115U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSCL_115U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSCL_115U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_115U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSCL_115U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSCL_115U_STR", detail));

            List<Map<String, Object>> raw = hsclMapper.HSCL_115U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("00000000".equals(String.valueOf(resRow.getOrDefault("slipymd", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("slipno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    // ==========================================
    // 3. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HSCL_200S_STR")
    public ResponseEntity<?> callHSCL_200S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_200S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSCL_200S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_200S_STR(params)));
    }

    @PostMapping("/HSCL_210S_STR")
    public ResponseEntity<?> callHSCL_210S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_210S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSCL_210S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_210S_STR(params)));
    }

    @PostMapping("/HSCL_220S_STR")
    public ResponseEntity<?> callHSCL_220S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_220S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSCL_220S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_220S_STR(params)));
    }

    @PostMapping("/HSCL_270S_STR")
    public ResponseEntity<?> callHSCL_270S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_270S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSCL_270S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_270S_STR(params)));
    }

    @PostMapping("/HSCL_290S_STR")
    public ResponseEntity<?> callHSCL_290S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_290S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSCL_290S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_290S_STR(params)));
    }

    @PostMapping("/HSCL_310S_STR")
    public ResponseEntity<?> callHSCL_310S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_310S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSCL_310S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_310S_STR(params)));
    }

    @PostMapping("/HSCL_520S_STR")
    public ResponseEntity<?> callHSCL_520S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSCL_520S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSCL_520S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsclMapper.HSCL_520S_STR(params)));
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
            String statementId = HsclMapper.class.getName() + "." + proc;
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
            String statementId = HsclMapper.class.getName() + "." + proc;
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
