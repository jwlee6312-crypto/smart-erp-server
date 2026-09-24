package com.crmbank.erp.hsod.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsod.dto.Hsod100uRequest;
import com.crmbank.erp.hsod.mapper.HsodMapper;
import com.crmbank.erp.hsod.service.HsodService;
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

/**
 * [HSOD] 수주관리 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hsod")
@RequiredArgsConstructor
public class HsodController {

    private final HsodMapper hsodMapper;
    private final HsodService hsodService;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. _SAVE 트랜잭션 서비스 보존
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/HSOD_100U_SAVE")
    public ResponseEntity<?> saveOrder(@RequestBody Hsod100uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();

        try {
            String userId = user.getUserid();
            String cmpycd = user.getCmpycd();

            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsodService.saveOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsod] Save Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HSOD_100U_STR")
    public ResponseEntity<?> callHSOD_100U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSOD_100U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSOD_100U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsodMapper.HSOD_100U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ordym", "ordno");
        String code = String.valueOf(resultRow.get("ordym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("ordno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSOD_101U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSOD_101U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSOD_101U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsodMapper.HSOD_101U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSOD_101U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSOD_101U_STR", detail));

            List<Map<String, Object>> raw = hsodMapper.HSOD_101U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("ordym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("ordno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HSOD_120U_STR")
    public ResponseEntity<?> callHSOD_120U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSOD_120U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSOD_120U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsodMapper.HSOD_120U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ordym", "ordno");
        String code = String.valueOf(resultRow.get("ordym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("ordno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSOD_200U_STR")
    public ResponseEntity<?> callHSOD_200U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSOD_200U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSOD_200U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsodMapper.HSOD_200U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSOD_210U_STR")
    public ResponseEntity<?> callHSOD_210U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSOD_210U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSOD_210U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsodMapper.HSOD_210U_STR(params);

        if ("S".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSOD_300U_STR")
    public ResponseEntity<?> callHSOD_300U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSOD_300U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSOD_300U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsodMapper.HSOD_300U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    // ==========================================
    // 3. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HSOD_110S_STR")
    public ResponseEntity<?> callHSOD_110S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSOD_110S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSOD_110S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsodMapper.HSOD_110S_STR(params)));
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
            String statementId = HsodMapper.class.getName() + "." + proc;
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
            String statementId = HsodMapper.class.getName() + "." + proc;
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
