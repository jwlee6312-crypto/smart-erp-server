package com.crmbank.erp.crm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.dto.*;
import com.crmbank.erp.comm.dto.*;
import com.crmbank.erp.crm.service.*;
import com.crmbank.erp.crm.mapper.inbound.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/crm/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;
    private final GeminiAiService geminiAiService;
    private final InboundMapper inboundMapper;

    @Value("${asterisk.sounds.path}")
    private String soundsPath;

    @Value("${asterisk.recording.path}")
    private String recordingPath;

    /**
     * 💡 Asterisk 지능형 하이브리드 라우팅 상태 체크 API
     */
    @GetMapping("/asterisk/check-routing")
    public ResponseEntity<String> checkRouting(@RequestParam String exten, @RequestParam(required = false) String cmpycd) {
        String finalCmpycd = (cmpycd == null || cmpycd.isEmpty()) ? "COIT" : cmpycd;
        String today = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());

        Map<String, Object> hParam = new HashMap<>();
        hParam.put("cmpycd", finalCmpycd);
        hParam.put("yymmdd", today);
        if (inboundMapper.checkHoliday(hParam) != null) {
            return getDutyRedirect(finalCmpycd, "HOLIDAY");
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        if (now.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || now.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            return getDutyRedirect(finalCmpycd, "WEEKEND");
        }

        java.time.LocalTime time = now.toLocalTime();
        if (time.isBefore(java.time.LocalTime.of(9, 0)) || time.isAfter(java.time.LocalTime.of(18, 0))) {
            return getDutyRedirect(finalCmpycd, "OFF_HOURS");
        }

        Map<String, Object> aParam = new HashMap<>();
        aParam.put("cmpycd", finalCmpycd);
        aParam.put("exten", exten);
        Map<String, Object> agent = inboundMapper.checkAgentStatus(aParam);

        if (agent == null || "N".equals(agent.get("useyn"))) {
            return ResponseEntity.ok("BLOCK:INVALID_AGENT");
        }

        String status = String.valueOf(agent.get("status"));
        String mobileNo = String.valueOf(agent.get("mobile_no"));

        if ("30".equals(status)) return ResponseEntity.ok("BLOCK:VACATION");
        if ("20".equals(status)) return ResponseEntity.ok("MOBILE_DIRECT:" + mobileNo);
        if ("40".equals(status)) return getDutyRedirect(finalCmpycd, "AGENT_OFF");

        return ResponseEntity.ok("OFFICE_FIRST:" + exten + ":" + mobileNo);
    }

    private ResponseEntity<String> getDutyRedirect(String cmpycd, String reason) {
        Map<String, Object> dParam = new HashMap<>();
        dParam.put("cmpycd", cmpycd);
        Map<String, Object> duty = inboundMapper.getDutyAgent(dParam);
        if (duty != null) {
            return ResponseEntity.ok("DUTY_DIRECT:" + duty.get("mobile_no") + ":" + reason);
        }
        return ResponseEntity.ok("BLOCK:" + reason);
    }

    @PostMapping("/log-callback")
    public ResponseEntity<Map<String, Object>> logCallback(@RequestBody Map<String, Object> params) {
        try {
            TotalCallLogDto logDto = TotalCallLogDto.builder()
                    .uniqueid(String.valueOf(params.get("interaction_id")))
                    .keyword(String.valueOf(params.get("keyword")))
                    .media_type(String.valueOf(params.get("media_type")))
                    .cmpycd(String.valueOf(params.get("cmpycd")))
                    .direction("in")
                    .start_time(LocalDateTime.now())
                    .callback_yn("Y")
                    .callback_no(String.valueOf(params.get("keyword")))
                    .callback_req_time(LocalDateTime.now())
                    .callback_retry_cnt(0)
                    .build();

            inboundService.insertTotalInteractionLog(logDto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pending-list")
    public List<Map<String, Object>> getPendingList(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        return toLowerCase(inboundMapper.selectCallbackList(params));
    }

    @GetMapping("/callback-list")
    public ResponseEntity<?> getCallbackList(@RequestParam Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();
        params.put("cmpycd", user.getCmpycd());
        return ResponseEntity.ok(toLowerCase(inboundMapper.selectCallbackList(params)));
    }

    @PostMapping("/interaction/save-response")
    public ResponseEntity<Map<String, Object>> saveCallbackResponse(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            UserSession user = (UserSession) session.getAttribute("user_session");
            String userid = user != null ? user.getUserid() : "system";
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("uniqueid", params.get("INTERACTION_ID"));
            updateParam.put("result_cd", params.get("rslt_cd"));
            updateParam.put("call_memo", params.get("remark"));
            updateParam.put("callback_agent_id", userid);
            updateParam.put("status", "300");
            inboundMapper.updateCallbackResult(updateParam);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 💡 상담 통합 저장 및 자동 STT/요약 (Async 및 KST 보정 적용)
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody SaveRequest request, HttpSession session) {
        try {
            UserSession user = (UserSession) session.getAttribute("user_session");
            String cmpycd = user != null ? user.getCmpycd() : "";
            String userid = user != null ? user.getUserid() : "system";
            String deptcd = user != null ? user.getDeptcd() : "";

            CallMstDto dto = request.getDto();
            dto.setCmpycd(cmpycd);
            dto.setConsultid(userid);
            dto.setUpdemp(userid);
            dto.setDeptcd(deptcd);
            dto.setHappycall_yn("N");
            
            // 🚀 한국 시간(KST) 강제 보정
            ZonedDateTime kstNow = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            dto.setEnd_time(kstNow.toLocalDateTime());

            if (dto.getInteraction_id() == null || dto.getInteraction_id().isEmpty()) {
                dto.setInteraction_id("IN_" + UUID.randomUUID().toString().substring(0, 8));
            }

            String lastFile = (request.getRecordings() != null && !request.getRecordings().isEmpty())
                    ? request.getRecordings().get(request.getRecordings().size() - 1) : null;
            if (lastFile != null) dto.setRec_file(lastFile);

            String svcymd = kstNow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String svcno = inboundService.saveCallMst(dto, request.getRecordings(), svcymd, deptcd);

            // 🤖 [AI 비동기 실행]
            if ("auto".equalsIgnoreCase(request.getAi_mode()) && lastFile != null) {
                String fullPath = new File(recordingPath, lastFile).getAbsolutePath();
                inboundService.processAiSummaryAsync(cmpycd, svcno, fullPath, userid);
            }

            return ResponseEntity.ok(Map.of("success", true, "svcno", svcno));
        } catch (Exception e) {
            log.error("상담 저장 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/ai-summarize")
    public Map<String, String> aiSummarize(@RequestBody Map<String, String> params) {
        String chatLog = "문의내용: " + params.get("trb_ment") + "\n답변내용: " + params.get("ans_ment");
        Map<String, String> result = new HashMap<>();
        result.put("summary", geminiAiService.summarizeText(chatLog));
        result.put("deptcd", "");
        return result;
    }

    @GetMapping("/play-recording")
    public ResponseEntity<ResourceRegion> playRecording(@RequestHeader HttpHeaders headers, @RequestParam(value = "file", required = false) String file) throws IOException {
        log.info("🎧 [음원 재생 요청] Parameter 'file': {}", file);
        
        if (file == null || file.trim().isEmpty()) {
            log.warn("🔈 [재생 실패] 파일명 파라미터가 누락되었습니다.");
            return ResponseEntity.badRequest().build();
        }

        // 🚀 [해결 핵심] 파일명 뒤에 붙은 캐시 방지용 쿼리 스트링(?t=...)을 제거합니다.
        String cleanFileName = file;
        if (cleanFileName.contains("?")) {
            cleanFileName = cleanFileName.substring(0, cleanFileName.indexOf("?"));
        }
        
        String fileNameOnly = new File(cleanFileName).getName();
        log.info("🔍 [경로 탐색] 최종 정제된 파일명: {}", fileNameOnly);
        
        // 🚀 1순위: TTS 음원(soundsPath), 2순위: 녹취(recordingPath)
        File targetFile = new File(soundsPath, fileNameOnly);
        if (!targetFile.exists()) {
            targetFile = new File(recordingPath, fileNameOnly);
        }

        if (!targetFile.exists()) {
            log.warn("❌ [파일 없음] 최종 경로 실패: {}, {}", 
                     new File(soundsPath, fileNameOnly).getAbsolutePath(),
                     new File(recordingPath, fileNameOnly).getAbsolutePath());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        log.info("✅ [재생 시작] 파일 발견: {} (크기: {} bytes)", targetFile.getAbsolutePath(), targetFile.length());

        FileSystemResource resource = new FileSystemResource(targetFile);
        long contentLength = resource.contentLength();
        
        List<HttpRange> ranges = headers.getRange();
        ResourceRegion region;
        if (!ranges.isEmpty()) {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024L, end - start + 1);
            region = new ResourceRegion(resource, start, rangeLength);
        } else {
            long rangeLength = Math.min(1024 * 1024L, contentLength);
            region = new ResourceRegion(resource, 0, rangeLength);
        }

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(region);
    }

    @GetMapping("/status-list")
    public List<Map<String, Object>> getStatusList(@RequestParam String fromdt, @RequestParam String todt, @RequestParam(required = false) String custnm, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        return inboundService.getStatusList(cmpycd, fromdt, todt, custnm);
    }

    @GetMapping("/customer-detail")
    public Map<String, Object> getCustomerDetail(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        return inboundService.getCustomerByCustCd(cmpycd, custcd);
    }

    @GetMapping("/item-list")
    public List<Map<String, Object>> getItemList(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        return inboundService.getItemList(cmpycd, custcd);
    }

    @GetMapping("/call-history")
    public List<Map<String, Object>> getCallHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        return inboundService.getCallHistory(cmpycd, custcd);
    }

    @GetMapping("/service-history")
    public List<Map<String, Object>> getServiceHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        return inboundService.getServiceHistory(cmpycd, custcd);
    }

    @GetMapping("/settle-history")
    public List<Map<String, Object>> getSettleHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        return inboundService.getSettleHistory(cmpycd, custcd);
    }

    private List<Map<String, Object>> toLowerCase(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(map -> {
            Map<String, Object> lowerMap = new HashMap<>();
            map.forEach((k, v) -> lowerMap.put(k != null ? k.toLowerCase() : null, v));
            return lowerMap;
        }).collect(Collectors.toList());
    }

    @Data public static class SaveRequest { private CallMstDto dto; private List<String> recordings; private String ai_mode; }
}
