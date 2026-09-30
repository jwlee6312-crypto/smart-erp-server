package com.crmbank.erp.hsio.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsio.mapper.HsioMapper;
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
 * 📱 [HSIO] 모바일 바코드 스캔 전용 별도 분리 컨트롤러
 * (HsioController 분잡화 방지 및 무결성 보장용)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hsio")
@RequiredArgsConstructor
public class HsioBarcodeController {

    private final HsioMapper hsioMapper;
    private final SqlSession sqlSession;

    /** 1. 바코드 스캔 이력 저장 (HSIO104T_TBL) - IOGBN=100: 입고, 200: 출고 */
    @PostMapping("/HSIO_104U_SAVE")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> saveBarcodeScanHistory(@RequestBody Object inputDetails, HttpSession session) {
        Map<String, Object> params = null;
        if (inputDetails instanceof Map) {
            params = (Map<String, Object>) inputDetails;
        } else if (inputDetails instanceof List && !((List<?>) inputDetails).isEmpty()) {
            params = (Map<String, Object>) ((List<?>) inputDetails).get(0);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (inputDetails instanceof List) {
            list = (List<Map<String, Object>>) inputDetails;
        } else if (params != null) {
            list.add(params);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> row = list.get(i);
            injectSession(row, session);
            fillMissingParameters("INSERT_HSIO104T_BARCODE", row);

            // iogbn 기본값 보정 (100: 입고, 200: 출고)
            if (row.get("iogbn") == null || String.valueOf(row.get("iogbn")).trim().isEmpty()) {
                row.put("iogbn", "100");
            }
            if (row.get("srowno") == null || String.valueOf(row.get("srowno")).trim().isEmpty()) {
                row.put("srowno", String.format("%03d", i + 1));
            }

            log.info("📱 [Barcode Scan Save #{}]: {}", i + 1, row);
            hsioMapper.INSERT_HSIO104T_BARCODE(row);
            results.add(Map.of("res", "OK", "srowno", row.get("srowno")));
        }
        return ResponseEntity.ok(results);
    }

    // ==========================================
    // 3. 공통 유틸리티 헬퍼
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

    private void fillMissingParameters(String statementName, Map<String, Object> params) {
        try {
            String statementId = HsioMapper.class.getName() + "." + statementName;
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
                }
            }
        } catch (Exception e) { log.warn("🛠 missing parameter alarm: {}", e.getMessage()); }
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