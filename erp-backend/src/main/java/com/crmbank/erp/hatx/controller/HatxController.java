package com.crmbank.erp.hatx.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hatx.mapper.HatxMapper;
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
 * [HATX] 부가세/세금계산서 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hatx")
@RequiredArgsConstructor
public class HatxController {

    private final HatxMapper hatxMapper;
    private final SqlSession sqlSession;

    // ==========================================
    // 1. _SAVE 통합 저장 트랜잭션 서비스
    // ==========================================

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/save-purchase")
    public ResponseEntity<?> savePurchaseVat(@RequestBody Map<String, Object> payload, HttpSession session) {
        if (session.getAttribute("user_session") == null) return ResponseEntity.status(401).build();
        UserSession user = (UserSession) session.getAttribute("user_session");

        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            String slipYn = String.valueOf(payload.getOrDefault("slipyn", "N"));
            String cmpycd = user.getCmpycd();
            String userid = user.getUserid();

            String slipymd = String.valueOf(master.getOrDefault("slipymd", "00000000")).replace("-", "");
            String slipno = String.valueOf(master.getOrDefault("slipno", ""));
            String srowno = "";

            if ("Y".equals(slipYn)) {
                String slipAct = (slipno == null || slipno.trim().isEmpty() || "000".equals(slipno)) ? "A" : "U";
                Map<String, Object> m = new HashMap<>();
                m.put("actkind", slipAct); m.put("cmpycd", cmpycd); m.put("slipymd", slipymd); m.put("slipno", slipno);
                m.put("deptcd", master.get("deptcd")); m.put("usernm", user.getUsernm()); m.put("slipgu", "010");
                m.put("remark", master.get("custnm") + " 매입건(공급가:" + master.get("supyamt") + " 부가세:" + master.get("vatamt") + ")");
                m.put("userid", userid);
                List<Map<String, Object>> mstRes = hatxMapper.HATX_01AU_STR(m);
                if (mstRes != null && !mstRes.isEmpty()) slipno = String.valueOf(mstRes.get(0).get("col_0"));

                double supyAmt = Double.parseDouble(String.valueOf(master.getOrDefault("supyamt", "0")).replace(",", ""));
                double vatAmt = Double.parseDouble(String.valueOf(master.getOrDefault("vatamt", "0")).replace(",", ""));

                // 차변 (매입액)
                Map<String, Object> db1 = new HashMap<>(master);
                db1.put("actkind", "A"); db1.put("cmpycd", cmpycd); db1.put("slipymd", slipymd); db1.put("slipno", slipno);
                db1.put("dbamt", String.valueOf((long)supyAmt)); db1.put("cramt", "0"); db1.put("updemp", userid);
                hatxMapper.HATX_01BU_STR(db1);

                // 차변 (매입부가세 - 1275)
                Map<String, Object> db2 = new HashMap<>(master);
                db2.put("actkind", "A"); db2.put("cmpycd", cmpycd); db2.put("slipymd", slipymd); db2.put("slipno", slipno);
                db2.put("acctcd", "1275"); db2.put("dbamt", String.valueOf((long)vatAmt)); db2.put("cramt", "0");
                db2.put("docno1", master.get("taxunit")); db2.put("docno2", master.get("custcd"));
                db2.put("docno3", master.get("taxtype")); db2.put("docno4", master.get("pubymd"));
                db2.put("docno5", master.get("supyamt")); db2.put("docno6", master.get("pubymd"));
                db2.put("updemp", userid);
                List<Map<String, Object>> detRes = hatxMapper.HATX_01BU_STR(db2);
                if (detRes != null && !detRes.isEmpty()) srowno = String.valueOf(detRes.get(0).get("col_0"));

                // 대변 (지불)
                for (int i = 1; i <= 2; i++) {
                    String payCndt = String.valueOf(master.getOrDefault("cpaycndt" + i, "000"));
                    if (payCndt.length() > 3) {
                        double inAmt = Double.parseDouble(String.valueOf(master.getOrDefault("cinamt" + i, "0")).replace(",", ""));
                        Map<String, Object> cr = new HashMap<>();
                        cr.put("actkind", master.getOrDefault("crowact" + i, "A"));
                        cr.put("cmpycd", cmpycd); cr.put("slipymd", slipymd); cr.put("slipno", slipno); cr.put("srowno", srowno);
                        cr.put("acctcd", master.get("cacctcd" + i)); cr.put("deptcd", master.get("deptcd"));
                        cr.put("custcd", master.get("custcd")); cr.put("mgtno", master.get("cmgtno" + i));
                        cr.put("dbamt", (supyAmt < 0 && inAmt > 0) ? String.valueOf((long)inAmt) : "0");
                        cr.put("cramt", (supyAmt < 0 && inAmt > 0) ? "0" : String.valueOf((long)inAmt));
                        cr.put("remark", master.get("descnm")); cr.put("paycndt", master.get("cpaytype" + i));
                        cr.put("payymd", String.valueOf(master.getOrDefault("cpayymd" + i, "00000000")).replace("-", ""));
                        cr.put("docno6", String.valueOf(master.getOrDefault("cstdymd" + i, "00000000")).replace("-", ""));
                        cr.put("docno7", String.valueOf(master.getOrDefault("cendymd" + i, "00000000")).replace("-", ""));
                        cr.put("docno8", String.valueOf((long)inAmt)); cr.put("docno9", master.get("custcd"));
                        cr.put("updemp", userid);
                        hatxMapper.HATX_01BU_STR(cr);
                    }
                }
            }

            Map<String, Object> vatMstParams = new HashMap<>(master);
            vatMstParams.put("cmpycd", cmpycd); vatMstParams.put("taxkind", "100"); vatMstParams.put("userid", userid);
            vatMstParams.put("slipymd", slipymd); vatMstParams.put("slipno", slipno); vatMstParams.put("srowno", srowno);
            vatMstParams.put("pubymd", String.valueOf(master.get("pubymd")).replace("-", ""));
            List<Map<String, Object>> vatRes = hatxMapper.HATX_010U_STR(vatMstParams);
            String taxym = String.valueOf(vatRes.get(0).get("col_0"));
            String taxno = String.valueOf(vatRes.get(0).get("col_1"));

            for (Map<String, Object> item : items) {
                if (item.get("itemnm") != null && !String.valueOf(item.get("itemnm")).trim().isEmpty()) {
                    item.put("actkind", "I1"); item.put("cmpycd", cmpycd); item.put("taxkind", "100");
                    item.put("taxym", taxym); item.put("taxno", taxno);
                    item.put("ymd", String.valueOf(item.get("ymd")).replace("-", ""));
                    hatxMapper.HATX_011U_STR(item);
                }
            }
            return ResponseEntity.ok(Map.of("res", "OK", "taxym", taxym, "taxno", taxno));
        } catch (Exception e) {
            log.error("❌ [savePurchase] Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/save-sales")
    public ResponseEntity<?> saveSalesVat(@RequestBody Map<String, Object> payload, HttpSession session) {
        if (session.getAttribute("user_session") == null) return ResponseEntity.status(401).build();
        UserSession user = (UserSession) session.getAttribute("user_session");

        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            String slipYn = String.valueOf(payload.getOrDefault("slipyn", "N"));
            String cmpycd = user.getCmpycd();
            String userid = user.getUserid();

            String slipymd = String.valueOf(master.getOrDefault("slipymd", "00000000")).replace("-", "");
            String slipno = String.valueOf(master.getOrDefault("slipno", ""));
            String srowno = "";

            if ("Y".equals(slipYn)) {
                String slipAct = (slipno == null || slipno.trim().isEmpty() || "000".equals(slipno)) ? "A" : "U";
                Map<String, Object> m = new HashMap<>();
                m.put("actkind", slipAct); m.put("cmpycd", cmpycd); m.put("slipymd", slipymd); m.put("slipno", slipno);
                m.put("deptcd", master.get("deptcd")); m.put("usernm", user.getUsernm()); m.put("slipgu", "010");
                m.put("remark", master.get("custnm") + " 매출건(공급가:" + master.get("supyamt") + " 부가세:" + master.get("vatamt") + ")");
                m.put("userid", userid);
                List<Map<String, Object>> mstRes = hatxMapper.HATX_01AU_STR(m);
                if (mstRes != null && !mstRes.isEmpty()) slipno = String.valueOf(mstRes.get(0).get("col_0"));

                double supyAmt = Double.parseDouble(String.valueOf(master.getOrDefault("supyamt", "0")).replace(",", ""));
                double vatAmt = Double.parseDouble(String.valueOf(master.getOrDefault("vatamt", "0")).replace(",", ""));

                // 차변 (입금 1, 2)
                for (int i = 1; i <= 2; i++) {
                    String payCndt = String.valueOf(master.getOrDefault("cpaycndt" + i, "000"));
                    if (payCndt.length() > 3) {
                        double inAmt = Double.parseDouble(String.valueOf(master.getOrDefault("cinamt" + i, "0")).replace(",", ""));
                        Map<String, Object> db = new HashMap<>();
                        db.put("actkind", master.getOrDefault("crowact" + i, "A"));
                        db.put("cmpycd", cmpycd); db.put("slipymd", slipymd); db.put("slipno", slipno);
                        db.put("acctcd", master.get("cacctcd" + i)); db.put("deptcd", master.get("deptcd"));
                        db.put("custcd", master.get("custcd")); db.put("prjcd", master.get("prjcd")); db.put("mgtno", master.get("cmgtno" + i));
                        db.put("dbamt", (supyAmt < 0 && inAmt > 0) ? "0" : String.valueOf((long)inAmt));
                        db.put("cramt", (supyAmt < 0 && inAmt > 0) ? String.valueOf((long)inAmt) : "0");
                        db.put("remark", master.get("descnm"));
                        db.put("docno6", String.valueOf(master.getOrDefault("cstdymd" + i, "00000000")).replace("-", ""));
                        db.put("docno7", String.valueOf(master.getOrDefault("cendymd" + i, "00000000")).replace("-", ""));
                        db.put("docno8", String.valueOf((long)inAmt)); db.put("docno9", master.get("custcd"));
                        db.put("updemp", userid);
                        hatxMapper.HATX_01BU_STR(db);
                    }
                }

                // 대변 (매출액)
                Map<String, Object> cr1 = new HashMap<>(master);
                cr1.put("actkind", "A"); cr1.put("cmpycd", cmpycd); cr1.put("slipymd", slipymd); cr1.put("slipno", slipno);
                cr1.put("dbamt", supyAmt < 0 ? String.valueOf((long)Math.abs(supyAmt)) : "0");
                cr1.put("cramt", supyAmt < 0 ? "0" : String.valueOf((long)supyAmt)); cr1.put("updemp", userid);
                hatxMapper.HATX_01BU_STR(cr1);

                // 대변 (매출부가세 - 2145)
                Map<String, Object> cr2 = new HashMap<>(master);
                cr2.put("actkind", "A"); cr2.put("cmpycd", cmpycd); cr2.put("slipymd", slipymd); cr2.put("slipno", slipno);
                cr2.put("acctcd", "2145");
                cr2.put("dbamt", vatAmt < 0 ? String.valueOf((long)Math.abs(vatAmt)) : "0");
                cr2.put("cramt", vatAmt < 0 ? "0" : String.valueOf((long)vatAmt));
                cr2.put("remark", master.get("descnm") + "(" + master.get("custnm") + ")");
                cr2.put("updemp", userid);
                List<Map<String, Object>> detRes = hatxMapper.HATX_01BU_STR(cr2);
                if (detRes != null && !detRes.isEmpty()) srowno = String.valueOf(detRes.get(0).get("col_0"));
            }

            Map<String, Object> v = new HashMap<>(master);
            v.put("cmpycd", cmpycd); v.put("taxkind", "200"); v.put("userid", userid);
            v.put("slipymd", slipymd); v.put("slipno", slipno); v.put("srowno", srowno);
            v.put("pubymd", String.valueOf(master.get("pubymd")).replace("-", ""));
            v.put("frgnrate", String.valueOf(master.getOrDefault("frgnrate", "0")).replace(",", ""));
            v.put("frgnamt", String.valueOf(master.getOrDefault("frgnamt", "0")).replace(",", ""));
            
            List<Map<String, Object>> vatRes = hatxMapper.HATX_010U_STR(v);
            String taxym = String.valueOf(vatRes.get(0).get("col_0"));
            String taxno = String.valueOf(vatRes.get(0).get("col_1"));

            for (Map<String, Object> item : items) {
                if (item.get("itemnm") != null && !String.valueOf(item.get("itemnm")).trim().isEmpty()) {
                    item.put("actkind", "I1"); item.put("cmpycd", cmpycd); item.put("taxkind", "200");
                    item.put("taxym", taxym); item.put("taxno", taxno);
                    item.put("ymd", String.valueOf(item.get("ymd")).replace("-", ""));
                    hatxMapper.HATX_011U_STR(item);
                }
            }
            return ResponseEntity.ok(Map.of("res", "OK", "taxym", taxym, "taxno", taxno));
        } catch (Exception e) {
            log.error("❌ [saveSales] Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/save-corrected-sales")
    public ResponseEntity<?> saveCorrectedSalesVat(@RequestBody Map<String, Object> payload, HttpSession session) {
        if (session.getAttribute("user_session") == null) return ResponseEntity.status(401).build();
        UserSession user = (UserSession) session.getAttribute("user_session");

        try {
            Map<String, Object> master = (Map<String, Object>) payload.get("master");
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            String slipYn = String.valueOf(payload.getOrDefault("slipyn", "N"));
            String cmpycd = user.getCmpycd();
            String userid = user.getUserid();

            String slipymd = String.valueOf(master.getOrDefault("slipymd", "00000000")).replace("-", "");
            String slipno = String.valueOf(master.getOrDefault("slipno", ""));
            String srowno = "";

            Map<String, Object> v = new HashMap<>(master);
            v.put("cmpycd", cmpycd); v.put("taxkind", "200"); v.put("userid", userid);
            v.put("pubymd", String.valueOf(master.get("pubymd")).replace("-", ""));
            v.put("bfymd", String.valueOf(master.get("bfymd")).replace("-", ""));
            
            List<Map<String, Object>> vatRes = hatxMapper.HATX_210U_STR(v);
            String taxym = String.valueOf(vatRes.get(0).get("col_0"));
            String taxno = String.valueOf(vatRes.get(0).get("col_1"));

            for (Map<String, Object> item : items) {
                if (item.get("itemnm") != null && !String.valueOf(item.get("itemnm")).trim().isEmpty()) {
                    item.put("actkind", "I1"); item.put("cmpycd", cmpycd); item.put("taxkind", "200");
                    item.put("taxym", taxym); item.put("taxno", taxno);
                    item.put("ymd", String.valueOf(item.get("ymd")).replace("-", ""));
                    hatxMapper.HATX_011U_STR(item);
                }
            }
            return ResponseEntity.ok(Map.of("res", "OK", "taxym", taxym, "taxno", taxno));
        } catch (Exception e) {
            log.error("❌ [saveCorrected] Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    @PostMapping("/HATX_010U_STR")
    public ResponseEntity<?> callHATX_010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HATX_010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hatxMapper.HATX_010U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "taxym", "taxno");
        String code = String.valueOf(resultRow.get("taxym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("taxno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HATX_011U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHATX_011U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HATX_011U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_011U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HATX_011U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HATX_011U_STR", detail));

            List<Map<String, Object>> raw = hatxMapper.HATX_011U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("taxym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("taxno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HATX_01AU_STR")
    public ResponseEntity<?> callHATX_01AU_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_01AU_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HATX_01AU_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hatxMapper.HATX_01AU_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("00000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HATX_01BU_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHATX_01BU_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HATX_01BU_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_01BU_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HATX_01BU_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HATX_01BU_STR", detail));

            List<Map<String, Object>> raw = hatxMapper.HATX_01BU_STR(detail);
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

    @PostMapping("/HATX_050U_STR")
    public ResponseEntity<?> callHATX_050U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_050U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HATX_050U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hatxMapper.HATX_050U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "cmpycd", "taxunit");
        String code = String.valueOf(resultRow.get("cmpycd")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("taxunit")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HATX_060U_STR")
    public ResponseEntity<?> callHATX_060U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_060U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HATX_060U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hatxMapper.HATX_060U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "taxunit", "taxkind");
        String code = String.valueOf(resultRow.get("taxunit")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("taxkind")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HATX_080U_STR")
    public ResponseEntity<?> callHATX_080U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_080U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HATX_080U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hatxMapper.HATX_080U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "taxunit", "deptcd");
        String code = String.valueOf(resultRow.get("taxunit")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("deptcd")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HATX_210U_STR")
    public ResponseEntity<?> callHATX_210U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_210U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HATX_210U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hatxMapper.HATX_210U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "taxym", "taxno");
        String code = String.valueOf(resultRow.get("taxym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("taxno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    // ==========================================
    // 3. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    @PostMapping("/HATX_030S_STR")
    public ResponseEntity<?> callHATX_030S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_030S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_030S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_030S_STR(params)));
    }

    @PostMapping("/HATX_040S_STR")
    public ResponseEntity<?> callHATX_040S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_040S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_040S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_040S_STR(params)));
    }

    @PostMapping("/HATX_110S_STR")
    public ResponseEntity<?> callHATX_110S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_110S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_110S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_110S_STR(params)));
    }

    @PostMapping("/HATX_130S_STR")
    public ResponseEntity<?> callHATX_130S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_130S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_130S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_130S_STR(params)));
    }

    @PostMapping("/HATX_140S_STR")
    public ResponseEntity<?> callHATX_140S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_140S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_140S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_140S_STR(params)));
    }

    @PostMapping("/HATX_150S_STR")
    public ResponseEntity<?> callHATX_150S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_150S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_150S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_150S_STR(params)));
    }

    @PostMapping("/HATX_160S_STR")
    public ResponseEntity<?> callHATX_160S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_160S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_160S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_160S_STR(params)));
    }

    @PostMapping("/HATX_170S_STR")
    public ResponseEntity<?> callHATX_170S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_170S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_170S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_170S_STR(params)));
    }

    @PostMapping("/HATX_500S_STR")
    public ResponseEntity<?> callHATX_500S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_500S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_500S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_500S_STR(params)));
    }

    @PostMapping("/HATX_600S_STR")
    public ResponseEntity<?> callHATX_600S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HATX_600S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HATX_600S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hatxMapper.HATX_600S_STR(params)));
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
            String statementId = HatxMapper.class.getName() + "." + proc;
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
            String statementId = HatxMapper.class.getName() + "." + proc;
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
