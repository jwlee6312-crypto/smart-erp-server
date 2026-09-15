package com.crmbank.erp.comm.service;

import com.crmbank.erp.comm.mapper.BriefingDashboardMapper;
import com.crmbank.erp.crm.service.GeminiAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BriefingDashboardService {

    private final BriefingDashboardMapper briefingMapper;

    public Map<String, Object> getBriefingData(String cmpycd) {
        Map<String, Object> params = Map.of("cmpycd", cmpycd);
        Map<String, Object> result = new HashMap<>();

        result.put("stats", briefingMapper.selectBriefingStats(params));
        result.put("feed", briefingMapper.selectBriefingFeed(params));
        result.put("notices", briefingMapper.selectBriefingNotices(params));
        result.put("expiry", briefingMapper.selectExpiryAlarms(params));

        return result;
    }

}
