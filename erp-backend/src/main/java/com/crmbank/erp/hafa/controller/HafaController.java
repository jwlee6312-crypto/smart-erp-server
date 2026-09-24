package com.crmbank.erp.hafa.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hafa.mapper.HafaMapper;
import com.crmbank.erp.hafa.service.HafaService;
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
 * [HAFA] 고정자산관리 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hafa")
@RequiredArgsConstructor
public class HafaController {

    private final HafaMapper hafaMapper;
    private final HafaService hafaService;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. _SAVE 트랜잭션 서비스
    // ==========================================

    @PostMapping("/HAFA_150U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveHafa150U(@RequestBody Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        injectSession(params, session);
        try {
            Map<String, Object> result = hafaService.saveDepreciationSlip(params);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hafa] HAFA_150U_SAVE Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HAFA_010U_STR")
    public ResponseEntity<?> callHAFA_010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFA_010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hafaMapper.HAFA_010U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ym", "msg");
        String code = String.valueOf(resultRow.get("ym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAFA_050U_STR")
    public ResponseEntity<?> callHAFA_050U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_050U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFA_050U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hafaMapper.HAFA_050U_STR(params);

        if ("S".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "asetcd", "msg");
        String code = String.valueOf(resultRow.get("asetcd")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAFA_090U_STR")
    public ResponseEntity<?> callHAFA_090U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_090U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFA_090U_STR", params));
        List<Map<String, Object>> raw = hafaMapper.HAFA_090U_STR(params);

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAFA_150U_STR")
    public ResponseEntity<?> callHAFA_150U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_150U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFA_150U_STR", params));
        List<Map<String, Object>> raw = hafaMapper.HAFA_150U_STR(params);

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HAFA_900U_STR")
    public ResponseEntity<?> callHAFA_900U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_900U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HAFA_900U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hafaMapper.HAFA_900U_STR(params);

        if ( "S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    // ==========================================
    // 3. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HAFA_020S_STR")
    public ResponseEntity<?> callHAFA_020S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_020S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFA_020S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafaMapper.HAFA_020S_STR(params)));
    }

    @PostMapping("/HAFA_040S_STR")
    public ResponseEntity<?> callHAFA_040S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_040S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFA_040S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafaMapper.HAFA_040S_STR(params)));
    }

    @PostMapping("/HAFA_120S_STR")
    public ResponseEntity<?> callHAFA_120S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_120S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFA_120S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafaMapper.HAFA_120S_STR(params)));
    }

    @PostMapping("/HAFA_130S_STR")
    public ResponseEntity<?> callHAFA_130S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_130S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFA_130S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafaMapper.HAFA_130S_STR(params)));
    }

    @PostMapping("/HA00_150S_STR")
    public ResponseEntity<?> callHA00_150S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HA00_150S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HA00_150S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafaMapper.HA00_150S_STR(params)));
    }

    @PostMapping("/HAFA_140S_STR")
    public ResponseEntity<?> callHAFA_140S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HAFA_140S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HAFA_140S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hafaMapper.HAFA_140S_STR(params)));
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
            if (params.get("usernm") == null || params.get("usernm").toString().trim().isEmpty()) {
                params.put("usernm", user.getUsernm());
            }
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HafaMapper.class.getName() + "." + proc;
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
            String statementId = HafaMapper.class.getName() + "." + proc;
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
