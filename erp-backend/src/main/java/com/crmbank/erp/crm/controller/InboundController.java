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
     * 📞 Asterisk 지능형 하이브리드 라우팅 상태 체크 API
     */
    @GetMapping("/asterisk/check-routing")
    public ResponseEntity<String> checkRouting(@RequestParam String exten, @RequestParam(required = false) String cmpycd) {
        String finalCmpycd = (cmpycd == null || cmpycd.isEmpty()) ? "COIT" : cmpycd;
        String today = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());

        Map<String, Object> hParam = new HashMap<>();
        hParam.put("cmpycd", finalCmpycd); hParam.put("yymmdd", today);
        if (inboundMapper.checkHoliday(hParam) != null) return getDutyRedirect(finalCmpycd, "HOLIDAY");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        if (now.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || now.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) return getDutyRedirect(finalCmpycd, "WEEKEND");
        
        java.time.LocalTime time = now.toLocalTime();
        if (time.isBefore(java.time.LocalTime.of(9, 0)) || time.isAfter(java.time.LocalTime.of(18, 0))) return getDutyRedirect(finalCmpycd, "OFF_HOURS");

        Map<String, Object> aParam = new HashMap<>(); aParam.put("cmpycd", finalCmpycd); aParam.put("exten", exten);
        Map<String, Object> agent = inboundMapper.checkAgentStatus(aParam);
        if (agent == null || "N".equals(agent.get("useyn"))) return ResponseEntity.ok("BLOCK:INVALID_AGENT");

        String status = String.valueOf(agent.get("status"));
        String mobileNo = String.valueOf(agent.get("mobile_no"));
        if ("30".equals(status)) return ResponseEntity.ok("BLOCK:VACATION");
        if ("20".equals(status)) return ResponseEntity.ok("MOBILE_DIRECT:" + mobileNo);
        if ("40".equals(status)) return getDutyRedirect(finalCmpycd, "AGENT_OFF");

        return ResponseEntity.ok("OFFICE_FIRST:" + exten + ":" + mobileNo);
    }

    private ResponseEntity<String> getDutyRedirect(String cmpycd, String reason) {
        Map<String, Object> dParam = new HashMap<>(); dParam.put("cmpycd", cmpycd);
        Map<String, Object> duty = inboundMapper.getDutyAgent(dParam);
        return (duty != null) ? ResponseEntity.ok("DUTY_DIRECT:" + duty.get("mobile_no") + ":" + reason) : ResponseEntity.ok("BLOCK:" + reason);
    }

    @PostMapping("/log-callback")
    public ResponseEntity<Map<String, Object>> logCallback(@RequestBody Map<String, Object> params) {
        try {
            log.info("📞 [ARS CALLBACK] 요청 수신: {}", params);

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
            log.error("❌ 콜백 로그 저장 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 💡 미결/콜백 리스트 조회 (HGOA200U)
     */
    @GetMapping("/pending-list")
    public List<Map<String, Object>> getPendingList(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        return toLowerCase(inboundMapper.selectCallbackList(params));
    }

    /**
     * 💡 콜백 통합 관리 리스트 조회 (HGOA110U)
     */
    @GetMapping("/callback-list")
    public ResponseEntity<?> getCallbackList(@RequestParam Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();

        params.put("cmpycd", user.getCmpycd());
        return ResponseEntity.ok(toLowerCase(inboundMapper.selectCallbackList(params)));
    }

    /**
     * 💡 콜백 응대 결과 저장 (HGOA110U / MHGOA110U)
     */
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
            updateParam.put("status", "300"); // 완료 상태

            inboundMapper.updateCallbackResult(updateParam);

            return ResponseEntity.ok(Map.of("success", true, "message", "처리가 완료되었습니다."));
        } catch (Exception e) {
            log.error("콜백 결과 저장 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody SaveRequest request, HttpSession session) {
        try {
            UserSession user = (UserSession) session.getAttribute("user_session");
            String cmpycd = user != null ? user.getCmpycd() : "";
            String userid = user != null ? user.getUserid() : "system";
            CallMstDto dto = request.getDto();
            dto.setCmpycd(cmpycd); dto.setConsultid(userid); dto.setUpdemp(userid); dto.setDeptcd(user != null ? user.getDeptcd() : "");
            ZonedDateTime kstNow = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            dto.setEnd_time(kstNow.toLocalDateTime());
            String lastFile = (request.getRecordings() != null && !request.getRecordings().isEmpty()) ? request.getRecordings().get(request.getRecordings().size() - 1) : null;
            if (lastFile != null) dto.setRec_file(lastFile);
            String svcymd = kstNow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String svcno = inboundService.saveCallMst(dto, request.getRecordings(), svcymd, dto.getDeptcd());
            
            // 🚀 [해결 핵심] 백엔드의 중복 AI 호출을 제거하여 사용량 쿼터 50% 절감
            // 화면에서 이미 분석된 결과를 사용하므로 여기서 다시 호출할 이유가 없음

            return ResponseEntity.ok(Map.of("success", true, "svcno", svcno));
        } catch (Exception e) { return ResponseEntity.status(500).build(); }
    }

    @PostMapping("/recording-list-pop")
    public ResponseEntity<List<Map<String, Object>>> getRecordingListPop(@RequestBody Map<String, Object> params) {
        File dir = new File(recordingPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".wav"));
        List<Map<String, Object>> result = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            for (int i = 0; i < Math.min(files.length, 20); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("filename", files[i].getName());
                map.put("mtime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(files[i].lastModified()), ZoneId.of("Asia/Seoul"))));
                map.put("size", String.format("%.1f KB", files[i].length() / 1024.0));
                result.add(map);
            }
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ai-summarize")
    public Map<String, String> aiSummarize(@RequestBody Map<String, String> params) {
        String chatLog = "문의내용: " + params.get("trb_ment") + "\n답변내용: " + params.get("ans_ment");
        Map<String, String> result = new HashMap<>();
        result.put("summary", geminiAiService.summarizeText(chatLog));
        result.put("deptcd", "");
        return result;
    }

    @GetMapping("/analyze-audio")
    public ResponseEntity<Map<String, String>> analyzeAudio(@RequestParam(required = false) String file, HttpSession session) {
        String targetFileName = file;
        UserSession user = (UserSession) session.getAttribute("user_session");
        String customerPhone = (user != null) ? user.getHpno() : ""; // 🚀 [추가] 세션이나 파라미터에서 전화번호 확보 가능

        if (targetFileName == null || targetFileName.trim().isEmpty() || "null".equals(targetFileName)) {
            File dir = new File(recordingPath); File[] files = dir.listFiles((d, name) -> name.endsWith(".wav"));
            if (files != null && files.length > 0) { Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed()); targetFileName = files[0].getName(); }
        }
        if (targetFileName == null) return ResponseEntity.status(404).body(Map.of("summary", "파일 없음"));
        // 🚀 [해결] AI 엔진에 고객 전화번호 정보를 함께 전달하여 화자 분리 정밀도 향상
        return ResponseEntity.ok(geminiAiService.analyzeAudio(new File(recordingPath, targetFileName).getAbsolutePath(), customerPhone));
    }

    @GetMapping("/play-recording")
    public ResponseEntity<ResourceRegion> playRecording(@RequestHeader HttpHeaders headers, @RequestParam(value = "file", required = false) String file) throws IOException {
        String cleanFileName = file.contains("?") ? file.substring(0, file.indexOf("?")) : file;
        String fileNameOnly = new File(cleanFileName).getName();
        File targetFile = new File(soundsPath, fileNameOnly);
        if (!targetFile.exists()) targetFile = new File(recordingPath, fileNameOnly);
        if (!targetFile.exists()) return ResponseEntity.notFound().build();

        FileSystemResource resource = new FileSystemResource(targetFile);
        long contentLength = resource.contentLength();
        List<HttpRange> ranges = headers.getRange();
        if (!ranges.isEmpty()) {
            HttpRange range = ranges.get(0); long start = range.getRangeStart(contentLength); long end = range.getRangeEnd(contentLength);
            return ResponseEntity.status(206).contentType(MediaType.parseMediaType("audio/wav")).body(new ResourceRegion(resource, start, Math.min(1024*1024L, end - start + 1)));
        }
        return ResponseEntity.status(206).contentType(MediaType.parseMediaType("audio/wav")).body(new ResourceRegion(resource, 0, Math.min(1024*1024L, contentLength)));
    }

    @GetMapping("/status-list")
    public List<Map<String, Object>> getStatusList(@RequestParam String fromdt, @RequestParam String todt, @RequestParam(required = false) String custnm, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getStatusList(user != null ? user.getCmpycd() : "", fromdt, todt, custnm);
    }

    @GetMapping("/customer-detail")
    public Map<String, Object> getCustomerDetail(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getCustomerByCustCd(user != null ? user.getCmpycd() : "", custcd);
    }

    @GetMapping("/item-list")
    public List<Map<String, Object>> getItemList(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getItemList(user != null ? user.getCmpycd() : "", custcd);
    }

    @GetMapping("/call-history")
    public List<Map<String, Object>> getCallHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getCallHistory(user != null ? user.getCmpycd() : "", custcd);
    }

    @GetMapping("/service-history")
    public List<Map<String, Object>> getServiceHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getServiceHistory(user != null ? user.getCmpycd() : "", custcd);
    }

    @GetMapping("/settle-history")
    public List<Map<String, Object>> getSettleHistory(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getSettleHistory(user != null ? user.getCmpycd() : "", custcd);
    }

    private List<Map<String, Object>> toLowerCase(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(map -> { Map<String, Object> lowerMap = new HashMap<>(); map.forEach((k, v) -> lowerMap.put(k != null ? k.toLowerCase() : null, v)); return lowerMap; }).collect(Collectors.toList());
    }

    @Data public static class SaveRequest { private CallMstDto dto; private List<String> recordings; private String ai_mode; }
}
