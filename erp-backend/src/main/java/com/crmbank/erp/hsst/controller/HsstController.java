package com.crmbank.erp.hsst.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsst.mapper.HsstMapper;
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
 * [HSST] 현황조회 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hsst")
@RequiredArgsConstructor
public class HsstController {

    private final HsstMapper hsstMapper;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HSST_100S_STR")
    public ResponseEntity<?> callHSST_100S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_100S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_100S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_100S_STR(params)));
    }

    @PostMapping("/HSST_120S_STR")
    public ResponseEntity<?> callHSST_120S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_120S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_120S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_120S_STR(params)));
    }

    @PostMapping("/HSST_130S_STR")
    public ResponseEntity<?> callHSST_130S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_130S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_130S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_130S_STR(params)));
    }

    @PostMapping("/HSST_150S_STR")
    public ResponseEntity<?> callHSST_150S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_150S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_150S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_150S_STR(params)));
    }

    @PostMapping("/HSST_180S_STR")
    public ResponseEntity<?> callHSST_180S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_180S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_180S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_180S_STR(params)));
    }

    @PostMapping("/HSST_200S_STR")
    public ResponseEntity<?> callHSST_200S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_200S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_200S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_200S_STR(params)));
    }

    @PostMapping("/HSST_210S_STR")
    public ResponseEntity<?> callHSST_210S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_210S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_210S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_210S_STR(params)));
    }

    @PostMapping("/HSST_300S_STR")
    public ResponseEntity<?> callHSST_300S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_300S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_300S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_300S_STR(params)));
    }

    @PostMapping("/HSST_320S_STR")
    public ResponseEntity<?> callHSST_320S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_320S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_320S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_320S_STR(params)));
    }

    @PostMapping("/HSST_340S_STR")
    public ResponseEntity<?> callHSST_340S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_340S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_340S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_340S_STR(params)));
    }

    @PostMapping("/HSST_360S_STR")
    public ResponseEntity<?> callHSST_360S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_360S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_360S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_360S_STR(params)));
    }

    @PostMapping("/HSST_510S_STR")
    public ResponseEntity<?> callHSST_510S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_510S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_510S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_510S_STR(params)));
    }

    @PostMapping("/HSST_520S_STR")
    public ResponseEntity<?> callHSST_520S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_520S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_520S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_520S_STR(params)));
    }

    @PostMapping("/HSST_570S_STR")
    public ResponseEntity<?> callHSST_570S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_570S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_570S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_570S_STR(params)));
    }

    @PostMapping("/HSST_600S_STR")
    public ResponseEntity<?> callHSST_600S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_600S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_600S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_600S_STR(params)));
    }

    @PostMapping("/HSST_610S_STR")
    public ResponseEntity<?> callHSST_610S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSST_610S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSST_610S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsstMapper.HSST_610S_STR(params)));
    }

    // ==========================================
    // 2. 공통 유틸리티 헬퍼 메서드
    // ==========================================

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
            String statementId = HsstMapper.class.getName() + "." + proc;
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
            String statementId = HsstMapper.class.getName() + "." + proc;
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
