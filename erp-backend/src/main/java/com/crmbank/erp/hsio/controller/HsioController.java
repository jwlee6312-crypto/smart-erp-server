package com.crmbank.erp.hsio.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsio.dto.*;
import com.crmbank.erp.hsio.mapper.HsioMapper;
import com.crmbank.erp.hsio.service.HsioService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/hsio")
@RequiredArgsConstructor
public class HsioController {

    private final HsioMapper hsioMapper;
    private final HsioService hsioService;
    private final SqlSession sqlSession;

    private final ObjectMapper objectMapper;

    @PostMapping("/HSIO_010U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveRequest(@RequestBody Hsio010uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveRequest(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveRequest Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_050U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOrder(@RequestBody Hsio050uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveOrder Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_052U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveGeneralOrder(@RequestBody Hsio052uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveGeneralOrder(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveGeneralOrder Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_500U_SAVE")
    public ResponseEntity<ApiResponse<?>> savePurchase(@RequestBody Hsio500uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.savePurchase(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] savePurchase Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    @PostMapping("/HSIO_550U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOutbound550(@RequestBody Hsio550uSaveRequest request, HttpSession session) throws Exception {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();

        if (request.getMst() != null) {
            request.getMst().setCmpycd(cmpycd);
            request.getMst().setUpdemp(userId);
        }
        // 🚀 [무결성 보장] 컨트롤러-서비스 전체를 하나의 트랜잭션으로 묶음
        Map<String, Object> result = hsioService.saveOutbound550(request, userId);
        return ResponseEntity.ok(ApiResponse.success(result, "출고 처리가 완료되었습니다."));
    }

    @PostMapping("/HSIO_060U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveInbound060(@RequestBody Hsio060uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.saveInbound060(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "입고 처리가 완료되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveInbound060 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_130U_SAVE")
    public ResponseEntity<ApiResponse<?>> generateSlip130(@RequestBody Hsio130uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.generateSlip130(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "매입전표가 성공적으로 발행되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] generateSlip130 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_131U_SAVE")
    public ResponseEntity<ApiResponse<?>> generateSlip131(@RequestBody Hsio131uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.generateSlip131(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "외부매입전표가 성공적으로 발행되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] generateSlip131 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_140U_CANCEL")
    public ResponseEntity<ApiResponse<?>> cancelSlips140(@RequestBody Hsio140uCancelRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.cancelSlips140(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "전표 취소가 완료되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] cancelSlips140 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_530U_SAVE")
    public ResponseEntity<ApiResponse<?>> generateSlip530(@RequestBody Hsio530uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.generateSlip530(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "매출전표가 성공적으로 발행되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] generateSlip530 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_540U_CANCEL")
    public ResponseEntity<ApiResponse<?>> cancelSlips540(@RequestBody Hsio540uCancelRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.cancelSlips540(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "전표 취소가 완료되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] cancelSlips540 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_531U_SAVE")
    public ResponseEntity<ApiResponse<?>> generateSlip531(@RequestBody Hsio531uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.generateSlip531(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "외부매출전표가 성공적으로 발행되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] generateSlip531 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_541U_CANCEL")
    public ResponseEntity<ApiResponse<?>> cancelSlips541(@RequestBody Hsio541uCancelRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.cancelSlips541(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "전표 취소가 완료되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] cancelSlips541 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_325U_SAVE")
    public ResponseEntity<ApiResponse<?>> generateSlip325(@RequestBody Hsio325uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.generateSlip325(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "외부입금전표가 성공적으로 발행되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] generateSlip325 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_325U_CANCEL")
    public ResponseEntity<ApiResponse<?>> cancelSlips325(@RequestBody Hsio325uCancelRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.cancelSlips325(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "전표 취소가 완료되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] cancelSlips325 Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_190U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOtherIn(@RequestBody Hsio190uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveOtherIn(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveOtherIn Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_250U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveOtherOut(@RequestBody Hsio250uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveOtherOut(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveOtherOut Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_300U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveDeposit(@RequestBody Hsio300uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveDeposit(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveDeposit Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_320U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveDepositSlip(@RequestBody Hsio320uSaveRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveDepositSlip(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 전표가 발행되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveDepositSlip Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @Transactional(value = "erpTransactionManager", rollbackFor = Exception.class)
    @PostMapping("/HSIO_510U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveSettlement(@RequestBody Hsio510uRequest request, HttpSession session) throws Exception {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();

        if (request.getMst() != null) {
            request.getMst().setCmpycd(cmpycd);
            request.getMst().setUpdemp(userId);
        }
        // 🚀 [무결성 보장] A0 -> U0 루프 -> V0 전체의 원자적 처리를 위해 트랜잭션 적용
        Map<String, Object> result = hsioService.saveSettlement(request, userId);
        return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 정산 처리되었습니다."));
    }

    @PostMapping("/HSIO_590U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveBatchSettlement(@RequestBody Hsio590uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            Map<String, Object> result = hsioService.saveBatchSettlement(request, user.getUserid());
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 일괄 정산 처리되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveBatchSettlement Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_580U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveWarehouseTransfer(@RequestBody Hsio580uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveWarehouseTransfer(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveWarehouseTransfer Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_720U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveStockAdjustment(@RequestBody Hsio720uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveStockAdjustment(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveStockAdjustment Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_730U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveInventoryAdjustment(@RequestBody Hsio730Request request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveInventoryAdjustment(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveInventoryAdjustment Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_570U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveStoreInout(@RequestBody Hsio570uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveStoreInout(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveStoreInout Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_490U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveSalesReturn(@RequestBody Hsio490uRequest request, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        String userId = user.getUserid();
        String cmpycd = user.getCmpycd();
        try {
            if (request.getMst() != null) {
                request.getMst().setCmpycd(cmpycd);
                request.getMst().setUpdemp(userId);
            }
            Map<String, Object> result = hsioService.saveSalesReturn(request, userId);
            return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveSalesReturn Error: {}, Payload: {}", e.getMessage(), request);
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_600U_SAVE")
    public ResponseEntity<ApiResponse<?>> saveHSIO600U(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        try {
            payload.put("cmpycd", user.getCmpycd());
            payload.put("updemp", user.getUserid());
            Map<String, Object> result = hsioService.saveHSIO600U(payload);
            return ResponseEntity.ok(ApiResponse.success(result, "출고 처리가 완료되었습니다."));
        } catch (Exception e) {
            log.error("❌ [hsio] saveHSIO600U Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError(e.getMessage()));
        }
    }

    @PostMapping("/HSIO_010U_STR")
    public ResponseEntity<?> callHSIO_010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_010U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "reqym", "reqno");
        String code = String.valueOf(resultRow.get("reqym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("reqno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }


    /** 📑 상세 내역 등록 (그리드 멀티 행 반복 처리) */
    @PostMapping("/HSIO_011U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_011U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind) || "S1".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_011U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_011U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_011U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_011U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_011U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("reqym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("reqno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HSIO_020U_STR")
    public ResponseEntity<?> callHSIO_020U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_020U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_020U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_020U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "reqym", "reqno");
        String code = String.valueOf(resultRow.get("reqym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("reqno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    /** 📑 상세 내역 등록 (그리드 멀티 행 반복 처리) */
    @PostMapping("/HSIO_021U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_021U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {

        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_021U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_021U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_021U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_021U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_021U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("reqym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("reqno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HSIO_050U_STR")
    public ResponseEntity<?> callHSIO_050U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_050U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_050U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_050U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "balym", "balno");
        String code = String.valueOf(resultRow.get("balym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("balno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }


    /** 📑 상세 내역 등록 (그리드 멀티 행 반복 처리) */
    @PostMapping("/HSIO_051U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_051U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_051U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_051U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_051U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_051U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_051U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("balym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("balno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HSIO_052U_STR")
    public ResponseEntity<?> callHSIO_052U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_052U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_052U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_052U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));
        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "balym", "balno");
        String code = String.valueOf(resultRow.get("balym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("balno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    @PostMapping("/HSIO_060U_STR")
    public ResponseEntity<?> callHSIO_060U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_060U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_060U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_060U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }


    /** 📑 상세 내역 등록 (그리드 멀티 행 반복 처리) */
    @PostMapping("/HSIO_061U_STR")
    public ResponseEntity<?> callHSIO_061U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_061U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_061U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_061U_STR(detail);
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

    @PostMapping("/HSIO_070U_STR")
    public ResponseEntity<?> callHSIO_070U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_070U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_070U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_070U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_100U_STR")
    public ResponseEntity<?> callHSIO_100U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_100U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_100U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_100U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    /** 📑 상세 내역 등록 (그리드 멀티 행 반복 처리) */
    @PostMapping("/HSIO_101U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_101U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_101U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_101U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_101U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_101U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_101U_STR(detail);
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

    @PostMapping("/HSIO_130U_STR")
    public ResponseEntity<?> callHSIO_130U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_130U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_130U_STR", params));

        //A0,S0
        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_130U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    /** 📑 상세 내역 등록 (그리드 멀티 행 반복 처리) */
    @PostMapping("/HSIO_131U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_131U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S0".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_131U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_131U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_101U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_131U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_131U_STR(detail);
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

    @PostMapping("/HSIO_140U_STR")
    public ResponseEntity<?> callHSIO_140U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_140U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_140U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_140U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("00000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    /** 📑 상세 내역 등록 (그리드 멀티 행 반복 처리) */
    @PostMapping("/HSIO_141U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_141U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S0".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_141U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_141U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_141U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_141U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_141U_STR(detail);
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

    @PostMapping("/HSIO_170U_STR")
    public ResponseEntity<?> callHSIO_170U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_170U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_170U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_170U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if ("OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_171U_STR")
    public ResponseEntity<?> callHSIO_171U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_171U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_171U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_171U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("00000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_180U_STR")
    public ResponseEntity<?> callHSIO_180U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_180U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_180U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_180U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("00000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    @PostMapping("/HSIO_181U_STR")
    public ResponseEntity<?> callHSIO_181U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_181U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_181U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_181U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("0000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_190U_STR")
    public ResponseEntity<?> callHSIO_190U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_190U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_190U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_190U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }



    @PostMapping("/HSIO_191U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_191U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_191U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_191U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_191U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_191U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_101U_STR(detail);
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

    @PostMapping("/HSIO_250U_STR")
    public ResponseEntity<?> callHSIO_250U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_250U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_250U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_250U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }



    @PostMapping("/HSIO_251U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_251U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_251U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_251U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_251U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_251U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_251U_STR(detail);
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


    @PostMapping("/HSIO_300U_STR")
    public ResponseEntity<?> callHSIO_300U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_300U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_300U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_300U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "S1".equals(actkind) || "S2".equals(actkind)|| "L0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "imym", "imno");
        String code = String.valueOf(resultRow.get("imym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("imno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_490U_STR")
    public ResponseEntity<?> callHSIO_490U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_490U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_490U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_490U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_491U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_491U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_491U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_491U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_491U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_491U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_491U_STR(detail);
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


    @PostMapping("/HSIO_500U_STR")
    public ResponseEntity<?> callHSIO_500U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_500U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_500U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_500U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_501U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_501U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_501U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_501U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_501U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_501U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_501U_STR(detail);
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

    @PostMapping("/HSIO_301U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_301U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S0".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_301U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_301U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_301U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_301U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_301U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if ("000000".equals(String.valueOf(resRow.getOrDefault("imym", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("imno", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }

    @PostMapping("/HSIO_320U_STR")
    public ResponseEntity<?> callHSIO_320U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_320U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_320U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_320U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "imym", "imno");
        String code = String.valueOf(resultRow.get("imym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("imno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_325U_STR")
    public ResponseEntity<?> callHSIO_325U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_325U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_325U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_325U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_510U_STR")
    public ResponseEntity<?> callHSIO_510U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_510U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_510U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_510U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "imym", "imno");
        String code = String.valueOf(resultRow.get("imym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("imno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

     @PostMapping("/HSIO_520U_STR")
    public ResponseEntity<?> callHSIO_520U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_520U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_520U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_520U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "jsanym", "jsanno");
        String code = String.valueOf(resultRow.get("jsanym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("jsanno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_530U_STR")
    public ResponseEntity<?> callHSIO_530U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_530U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_530U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_530U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "jsanym", "jsanno");
        String code = String.valueOf(resultRow.get("jsanym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("jsanno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_540U_STR")
    public ResponseEntity<?> callHSIO_540U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_540U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_540U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_540U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if ("Y".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_541U_STR")
    public ResponseEntity<?> callHSIO_541U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_541U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_541U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_541U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("resu")).trim();
        if ("Y".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_550U_STR")
    public ResponseEntity<?> callHSIO_550U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_550U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_550U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_550U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_551U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_551U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_551U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_551U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_551U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_551U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_551U_STR(detail);
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

    @PostMapping("/HSIO_560U_STR")
    public ResponseEntity<?> callHSIO_560U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_560U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_560U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_560U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if ("Y".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_570U_STR")
    public ResponseEntity<?> callHSIO_570U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_570U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_570U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_570U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }



    @PostMapping("/HSIO_571U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_571U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_571U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_571U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_571U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_571U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_571U_STR(detail);
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

    @PostMapping("/HSIO_580U_STR")
    public ResponseEntity<?> callHSIO_580U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_580U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_580U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_580U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }


    @PostMapping("/HSIO_581U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_581U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_581U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_581U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_581U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_581U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_581U_STR(detail);
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



    @PostMapping("/HSIO_590U_STR")
    public ResponseEntity<?> callHSIO_590U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_590U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_590U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_590U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if ("Y".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_600U_STR")
    public ResponseEntity<?> callHSIO_600U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_600U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_600U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_590U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "outym", "outno");
        String code = String.valueOf(resultRow.get("outym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("outno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }


    @PostMapping("/HSIO_720U_STR")
    public ResponseEntity<?> callHSIO_720U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_720U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_720U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_720U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "ioym", "iono");
        String code = String.valueOf(resultRow.get("ioym")).trim();
        if ("000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("iono")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_721U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHSIO_721U_STR(@RequestBody List<Map<String, Object>> details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S")).toUpperCase();
            if ("S".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HSIO_721U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_721U_STR(params)));
            }
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        // 🚀 [해결] 디테일은 상식적으로 리스트(반복) 처리가 정석
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HSIO_721U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HSIO_721U_STR", detail));

            List<Map<String, Object>> raw = hsioMapper.HSIO_721U_STR(detail);
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

    @PostMapping("/HSIO_990U_STR")
    public ResponseEntity<?> callHSIO_990U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_990U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HSIO_990U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = hsioMapper.HSIO_990U_STR(params);

        // 🚀 [해결] actkind가 'S'(조회) 일 경우 반복문이나 에러 체크 없이 즉시 반환
        if ("S0".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        // 🚀 [해결] 마스터는 반복문 없이 첫 번째 행만 즉시 별칭 부여 및 검증
        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "slipymd", "slipno");
        String code = String.valueOf(resultRow.get("slipymd")).trim();
        if ("00000000".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("slipno")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HSIO_080S_STR")
    public ResponseEntity<?> callHSIO_080S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_080S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_080S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_080S_STR(params)));
    }

    @PostMapping("/HSIO_082S_STR")
    public ResponseEntity<?> callHSIO_082S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_082S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_082S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_082S_STR(params)));
    }

    @PostMapping("/HSIO_085S_STR")
    public ResponseEntity<?> callHSIO_085S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_085S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_085S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_085S_STR(params)));
    }
    @PostMapping("/HSIO_200S_STR")
    public ResponseEntity<?> callHSIO_200S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_200S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_200S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_200S_STR(params)));
    }
    @PostMapping("/HSIO_210S_STR")
    public ResponseEntity<?> callHSIO_210S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_210S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_210S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_210S_STR(params)));
    }
    @PostMapping("/HSIO_215S_STR")
    public ResponseEntity<?> callHSIO_215S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_215S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_215S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_215S_STR(params)));
    }
    @PostMapping("/HSIO_220S_STR")
    public ResponseEntity<?> callHSIO_220S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_220S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_220S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_220S_STR(params)));
    }

    @PostMapping("/HSIO_400S_STR")
    public ResponseEntity<?> callHSIO_400S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_400S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_400S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_400S_STR(params)));
    }
    @PostMapping("/HSIO_410S_STR")
    public ResponseEntity<?> callHSIO_410S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_410S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_410S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_410S_STR(params)));
    }
    @PostMapping("/HSIO_470S_STR")
    public ResponseEntity<?> callHSIO_470S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_470S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_470S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_470S_STR(params)));
    }

    @PostMapping("/HSIO_600S_STR")
    public ResponseEntity<?> callHSIO_600S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_600S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_600S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_600S_STR(params)));
    }
    @PostMapping("/HSIO_610S_STR")
    public ResponseEntity<?> callHSIO_610S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_610S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_610S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_610S_STR(params)));
    }
    @PostMapping("/HSIO_620S_STR")
    public ResponseEntity<?> callHSIO_620S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_620S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_620S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_620S_STR(params)));
    }
    @PostMapping("/HSIO_640S_STR")
    public ResponseEntity<?> callHSIO_640S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_640S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_640S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_640S_STR(params)));
    }
    @PostMapping("/HSIO_650S_STR")
    public ResponseEntity<?> callHSIO_650S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_650S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_650S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_650S_STR(params)));
    }
    @PostMapping("/HSIO_660S_STR")
    public ResponseEntity<?> callHSIO_660S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_660S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_660S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_660S_STR(params)));
    }

    @PostMapping("/HSIO_680S_STR")
    public ResponseEntity<?> callHSIO_680S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_680S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_680S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_680S_STR(params)));
    }
    @PostMapping("/HSIO_690S_STR")
    public ResponseEntity<?> callHSIO_690S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_690S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_690S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_690S_STR(params)));
    }

    @PostMapping("/HSIO_TRANS_STR")
    public ResponseEntity<?> callHSIO_TRANS_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_TRANS_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_TRANS_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_TRANS_STR(params)));
    }
    @PostMapping("/HSIO_REQOUT_STR")
    public ResponseEntity<?> callHSIO_REQOUT_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_REQOUT_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_REQOUT_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_REQOUT_STR(params)));
    }
    @PostMapping("/HSIO_REQIN_STR")
    public ResponseEntity<?> callHSIO_REQIN_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HSIO_REQIN_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HSIO_REQIN_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(hsioMapper.HSIO_REQIN_STR(params)));
    }

    private Map<String, Object> mapToAlias(Map<String, Object> rawRow, String col1Alias, String col2Alias) {
        Map<String, Object> newMap = new LinkedHashMap<>();
        int i = 1;
        for (Map.Entry<String, Object> entry : rawRow.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.startsWith("col") || key.isEmpty()) { if (i == 1) key = col1Alias; else if (i == 2) key = col2Alias; }
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
            String statementId = HsioMapper.class.getName() + "." + proc;
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

    private String validateParameters(String proc, Map<String, Object> vueParams) {
        try {
            String statementId = HsioMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return null;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(vueParams);
            List<ParameterMapping> xmlMappings = boundSql.getParameterMappings();
            Set<String> xmlKeys = new LinkedHashSet<>();
            for (ParameterMapping pm : xmlMappings) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) xmlKeys.add(prop);
            }
            Set<String> vueKeys = vueParams.keySet();
            if (vueKeys.size() < xmlKeys.size()) {
                return String.format("📍 [PARAM SHORTAGE] XML:%d > VUE:%d\n📋 [REQUIRED]: %s", xmlKeys.size(), vueKeys.size(), xmlKeys);
            }
            return null;
        } catch (Exception e) { return "VALIDATION ERROR: " + e.getMessage(); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HsioMapper.class.getName() + "." + proc;

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