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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/hfba")
@RequiredArgsConstructor
public class HfbaController {

    private final HfbaMapper hfbaMapper;
    private final SqlSession sqlSession;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<?> executeProcedure(
            @PathVariable String procedure,
            @RequestBody Map<String, Object> params,
            HttpSession session) {

        if (session.getAttribute("user_session") == null) {
            return ResponseEntity.status(401).build();
        }

        String proc = procedure.toUpperCase();
        try {
            // 1. 세션 정보 주입 (cmpycd, userid 등 필수값 보장)
            injectSession(params, session);
            fillMissingParameters(proc, params);

            log.info("📋 [hfba] 프로시저 실행 요청: {}", params);

            // 2. 🚀 [핵심 정리] 조잡한 if-else/jdbcTemplate 루틴을 싹 걷어냅니다.
            // 모든 단일 폼 프로시저(CRUD 통합)는 hfbaMapper가 가장 안전하게 실행합니다.
            List<Map<String, Object>> result = switch (proc) {
                case "FBA1010U_STR" -> hfbaMapper.FBA1010U_STR(params);
                case "FBA1040U_STR" -> hfbaMapper.FBA1040U_STR(params);
                case "FBA1060U_STR" -> hfbaMapper.FBA1060U_STR(params);
                case "FBA2010U_STR" -> hfbaMapper.FBA2010U_STR(params);
                case "FBA2020U_SEL" -> hfbaMapper.FBA2020U_SEL(params);
                case "FBA2020U_MOD" -> hfbaMapper.FBA2020U_MOD(params);
                case "FBA2020U_DEL" -> hfbaMapper.FBA2020U_DEL(params);
                case "FBA3010U_STR" -> hfbaMapper.FBA3010U_STR(params);
                case "SELECT_DIVIDE_LIST" -> hfbaMapper.selectDivideList(params);
                case "SELECT_ACCT_LIST" -> hfbaMapper.selectAcctList(params);
                case "SELECT_DIVIDE_JUKSU_LIST" -> hfbaMapper.selectDivideJuksuList(params);
                default -> {
                    log.error("❌ 미정의 프로시저 호출 시도: {}", proc);
                    yield null;
                }
            };

            if (result == null) return ResponseEntity.notFound().build();

            // 3. 결과 반환 처리 (프로시저가 리턴하는 RESULT, MSG가 자연스럽게 포함됨)
            if (result.isEmpty()) {
                String actkind = String.valueOf(params.getOrDefault("actkind", "")).toUpperCase();
                // 조회(S, L)가 아니면 기본 'OK' 신호를 리턴하여 프론트엔드 오류 방지
                if (!actkind.startsWith("S") && !actkind.startsWith("L")) {
                    result = List.of(Map.of("result", "Y", "msg", "정상 처리되었습니다."));
                }
            }

            return ResponseEntity.ok(convertToLowerCaseKeys(result));

        } catch (Exception e) {
            log.error("❌ [hfba] {} 실행 에러: {}", proc, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
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
            String statementId = "com.crmbank.erp.hfba.mapper.HfbaMapper." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);

            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String cleanProp = prop.trim();
                    if (!params.containsKey(cleanProp) ||
                         params.get(cleanProp) == null ||
                         "null".equals(String.valueOf(params.get(cleanProp)).trim()) || // 🚀 "null" 문자열 체크 추가
                         params.get(cleanProp).toString().trim().isEmpty()) {
                        params.put(cleanProp, "");
                    }
                    if (!cleanProp.equals(prop)) params.put(prop, params.get(cleanProp));
                }
            }
        } catch (Exception e) { log.warn("🛠 누락 파라미터 보정 중 알림 ({}): {}", proc, e.getMessage()); }
    }
}
