package com.crmbank.erp.hpio.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hpio.mapper.HpioMapper;
import com.crmbank.erp.hpio.service.HpioService;
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

@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hpio")
@RequiredArgsConstructor
public class HpioController {

    private final HpioMapper hpioMapper;
    private final HpioService hpioService;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. _SAVE 트랜잭션 서비스
    // ==========================================

    @PostMapping("/HPIO_250U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveHpio250U(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            payload.put("cmpycd", user.getCmpycd());
            payload.put("updemp", user.getUserid());
            Map<String, Object> result = hpioService.saveHpio250U(payload);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hpio] saveHpio250U Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일)
    // ==========================================

    @PostMapping("/HPIO_110U_STR")
    public ResponseEntity<?> callHPIO_110U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_110U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_110U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_110U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "jsanym", "jsanno");
        String code = String.valueOf(resultRow.get("jsanym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("jsanno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_200U_STR")
    public ResponseEntity<?> callHPIO_200U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_200U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_200U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_200U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_210U_STR")
    public ResponseEntity<?> callHPIO_210U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_210U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_210U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_210U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_250U_STR")
    public ResponseEntity<?> callHPIO_250U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_250U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_250U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_250U_STR(params);

        if ("S0".equals(actkind) || "S1".equals(actkind) || "S2".equals(actkind) || "L0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "outym", "outno");
        String code = String.valueOf(resultRow.get("outym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("outno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_251U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_251U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S0".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HPIO_251U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_251U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_251U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_251U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_251U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("outym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("outno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_253U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_253U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ( "S0".equals(actkind) ) {
                injectSession(params, session);
                fillMissingParameters("HPIO_253U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_253U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_253U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_253U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_253U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("outym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("outno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_290U_STR")
    public ResponseEntity<?> callHPIO_290U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_290U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_290U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_290U_STR(params);

        if ( "S0".equals(actkind) ||  "L0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "pumym", "pumno");
        String code = String.valueOf(resultRow.get("pumym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("pumno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_291U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_291U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ( "S0".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HPIO_291U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_291U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_291U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_291U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_291U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("pumym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("pumno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_292U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_292U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind) || "B".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HPIO_292U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_292U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_292U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_292U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_292U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_300U_STR")
    public ResponseEntity<?> callHPIO_300U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_300U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_300U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_300U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_301U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_301U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ( "S0".equals(actkind) ) {
                injectSession(params, session);
                fillMissingParameters("HPIO_301U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_301U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_301U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_301U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_301U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_340U_STR")
    public ResponseEntity<?> callHPIO_340U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_340U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_340U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_340U_STR(params);

        if ("S".equals(actkind) ||  "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "outym", "outno");
        String code = String.valueOf(resultRow.get("outym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("outno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_341U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_341U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind) || "B".equals(actkind) ) {
                injectSession(params, session);
                fillMissingParameters("HPIO_341U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_341U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_341U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_341U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_341U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("outym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("outno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_350U_STR")
    public ResponseEntity<?> callHPIO_350U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_350U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_350U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_350U_STR(params);

        if ( "S0".equals(actkind) ||  "L0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_351U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_351U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ( "S0".equals(actkind) ) {
                injectSession(params, session);
                fillMissingParameters("HPIO_351U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_351U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_351U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_351U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_351U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "msg")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_400U_STR")
    public ResponseEntity<?> callHPIO_400U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_400U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_400U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_400U_STR(params);

        if ("S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_410U_STR")
    public ResponseEntity<?> callHPIO_410U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_410U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_410U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_410U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_500U_STR")
    public ResponseEntity<?> callHPIO_500U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_500U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_500U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_500U_STR(params);

        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_501U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_501U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HPIO_501U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_501U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_501U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_501U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_501U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("ioym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("ono", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_510U_STR")
    public ResponseEntity<?> callHPIO_510U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_510U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_510U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_510U_STR(params);

        if ("S".equals(actkind) || "Q".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_511U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_511U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind) ) {
                injectSession(params, session);
                fillMissingParameters("HPIO_511U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_511U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_511U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_511U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_511U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("ioym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("iono", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_520U_STR")
    public ResponseEntity<?> callHPIO_520U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_520U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_520U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_520U_STR(params);

        if ("S".equals(actkind)  || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HPIO_521U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHPIO_521U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind) ) {
                injectSession(params, session);
                fillMissingParameters("HPIO_521U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_521U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HPIO_521U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HPIO_521U_STR", detail));

            List<Map<String, Object>> raw = hpioMapper.HPIO_521U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("outym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("outno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HPIO_870U_STR")
    public ResponseEntity<?> callHPIO_870U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_870U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HPIO_870U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hpioMapper.HPIO_870U_STR(params);

        if ("S2".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    // ==========================================
    // 3. S_STR 및 조회/팝업 프로시저 (직결 호출)
    // ==========================================

    @PostMapping("/HPIO_230S_STR")
    public ResponseEntity<?> callHPIO_230S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_230S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_230S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_230S_STR(params)));
    }

    @PostMapping("/HPIO_251S_STR")
    public ResponseEntity<?> callHPIO_251S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_251S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_251S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_251S_STR(params)));
    }

    @PostMapping("/HPIO_252S_STR")
    public ResponseEntity<?> callHPIO_252S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_252S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_252S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_252S_STR(params)));
    }

    @PostMapping("/HPIO_250U_POP")
    public ResponseEntity<?> callHPIO_250U_POP(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_250U_POP", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_250U_POP", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_250U_POP(params)));
    }

    @PostMapping("/HPIO_340U_POPUP")
    public ResponseEntity<?> callHPIO_340U_POPUP(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_340U_POPUP", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_340U_POPUP", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_340U_POPUP(params)));
    }

    @PostMapping("/HPIO_360S_STR")
    public ResponseEntity<?> callHPIO_360S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_360S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_360S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_360S_STR(params)));
    }

    @PostMapping("/HPIO_370S_STR")
    public ResponseEntity<?> callHPIO_370S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_370S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_370S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_370S_STR(params)));
    }

    @PostMapping("/HPIO_380S_STR")
    public ResponseEntity<?> callHPIO_380S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_380S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_380S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_380S_STR(params)));
    }

    @PostMapping("/HPIO_390S_STR")
    public ResponseEntity<?> callHPIO_390S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_390S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_390S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_390S_STR(params)));
    }

    @PostMapping("/HPIO_420S_STR")
    public ResponseEntity<?> callHPIO_420S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_420S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_420S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_420S_STR(params)));
    }

    @PostMapping("/HPIO_430S_STR")
    public ResponseEntity<?> callHPIO_430S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_430S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_430S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_430S_STR(params)));
    }

    @PostMapping("/HPIO_640S_STR")
    public ResponseEntity<?> callHPIO_640S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_640S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_640S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_640S_STR(params)));
    }

    @PostMapping("/HPIO_650S_STR")
    public ResponseEntity<?> callHPIO_650S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_650S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_650S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_650S_STR(params)));
    }

    @PostMapping("/HPIO_660S_STR")
    public ResponseEntity<?> callHPIO_660S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_660S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_660S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_660S_STR(params)));
    }

    @PostMapping("/HPIO_710S_STR")
    public ResponseEntity<?> callHPIO_710S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_710S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_710S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_710S_STR(params)));
    }

    @PostMapping("/HPIO_720S_STR")
    public ResponseEntity<?> callHPIO_720S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_720S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_720S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_720S_STR(params)));
    }

    @PostMapping("/HPIO_850S_STR")
    public ResponseEntity<?> callHPIO_850S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HPIO_850S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HPIO_850S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hpioMapper.HPIO_850S_STR(params)));
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
            String statementId = HpioMapper.class.getName() + "." + proc;
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
            String statementId = HpioMapper.class.getName() + "." + proc;
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
