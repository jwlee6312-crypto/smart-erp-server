package com.crmbank.erp.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.handler.*;
import com.crmbank.erp.crm.mapper.inbound.*;
import com.crmbank.erp.crm.dto.*;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.event.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsteriskService implements ManagerEventListener {

    private final ManagerConnection managerConnection;
    private final InboundMapper inboundMapper;
    private final CtiWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> exten_channel_map = new ConcurrentHashMap<>();
    private final Map<String, String> exten_caller_channel = new ConcurrentHashMap<>();
    private final Map<String, String> exten_linked_id = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> session_channels = new ConcurrentHashMap<>();
    private final Set<String> registered_extens = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Map<String, String> getExtenChannelMap() { return exten_channel_map; }
    public Map<String, String> getExtenLinkedId() { return exten_linked_id; }
    public Map<String, Set<String>> getSessionChannels() { return session_channels; }
    public List<String> getRegisteredExtens() { return new ArrayList<>(registered_extens); }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        if (event instanceof NewStateEvent) {
            handleNewState((NewStateEvent) event);
        } else if (event instanceof AgentCalledEvent) {
            handleAgentCalled((AgentCalledEvent) event);
        } else if (event instanceof BridgeEnterEvent) {
            handleBridgeEnter((BridgeEnterEvent) event);
        } else if (event instanceof HangupEvent) {
            handleHangup((HangupEvent) event);
        } else if (event instanceof NewChannelEvent) {
            handleNewChannel((NewChannelEvent) event);
        }
    }

    private void handleNewChannel(NewChannelEvent event) {
        String ext = extractNumberOnly(event.getChannel());
        if (ext != null) exten_channel_map.put(ext.trim(), event.getChannel());
    }

    private void handleNewState(NewStateEvent e) {
        String state = e.getChannelStateDesc();
        String channel = e.getChannel();
        String callerId = e.getCallerIdNum(); 
        String ext = extractNumberOnly(channel);
        
        if (ext == null || callerId == null) return;
        String cleanExt = ext.trim();

        if (exten_caller_channel.containsKey(cleanExt)) return;
        if (channel.contains("cti-answer") || channel.contains("Surrogate")) return;

        if ("Ringing".equals(state) && !cleanExt.equals(callerId.trim())) {
            if (callerId.length() < 3) return;
            log.info("📢 [POPUP] 신규 고객 전화 수신: {} <- {}", cleanExt, callerId);
            exten_caller_channel.put(cleanExt, channel);
            exten_linked_id.put(cleanExt, e.getUniqueId());
            sendInboundPopup(cleanExt, callerId, channel, e.getUniqueId());
        }
    }

    private void handleAgentCalled(AgentCalledEvent e) {
        String agentExt = extractNumberOnly(e.getInterface());
        if (agentExt != null) {
            String cleanExt = agentExt.trim();
            if (exten_caller_channel.containsKey(cleanExt)) return;
            log.info("📢 [POPUP] 큐 대기열 신호 수신: {} <- {}", cleanExt, e.getCallerIdNum());
            exten_caller_channel.put(cleanExt, e.getChannel());
            exten_linked_id.put(cleanExt, e.getLinkedId());
            sendInboundPopup(cleanExt, e.getCallerIdNum(), e.getChannel(), e.getLinkedId());
        }
    }

    private void handleBridgeEnter(BridgeEnterEvent e) {
        String exten = extractNumberOnly(e.getChannel());
        if (exten != null) {
            sendCtiEvent(exten.trim(), "CALL_CONNECTED", e.getChannel(), null);
        }
    }

    private void handleHangup(HangupEvent e) {
        String exten = extractNumberOnly(e.getChannel());
        if (exten != null) {
            String cleanExt = exten.trim();
            String recFile = exten_linked_id.get(cleanExt);
            sendCtiEvent(cleanExt, "CALL_HANGUP", e.getChannel(), recFile);
            exten_channel_map.remove(cleanExt);
            exten_caller_channel.remove(cleanExt);
            exten_linked_id.remove(cleanExt);
        }
    }

    public void answerCall(String exten) {
        if (exten == null) return;
        String cleanExt = exten.trim();
        String callerChannel = exten_caller_channel.get(cleanExt);
        if (callerChannel == null) return;
        try {
            managerConnection.sendAction(new SetVarAction(callerChannel, "AGENT_EXTEN", cleanExt));
            managerConnection.sendAction(new RedirectAction(callerChannel, "cti-answer-force", "s", 1));
        } catch (Exception e) {}
    }

    public void hangupCall(String exten, String ch) {
        if (exten == null) return;
        String cleanExt = exten.trim();
        try {
            managerConnection.sendAction(new HangupAction("SIP/" + cleanExt));
            String myCh = exten_channel_map.get(cleanExt);
            if (myCh != null) managerConnection.sendAction(new HangupAction(myCh));
            String callerCh = exten_caller_channel.get(cleanExt);
            if (callerCh != null) managerConnection.sendAction(new HangupAction(callerCh));
        } catch (Exception ex) {}
    }

    public boolean checkAmiConnection() {
        return managerConnection != null && managerConnection.getState() == org.asteriskjava.manager.ManagerConnectionState.CONNECTED;
    }

    private void sendInboundPopup(String exten, String callerId, String channel, String linkedId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "INBOUND_CALL");
            data.put("callerid", callerId);
            data.put("exten", exten);
            data.put("linkedid", linkedId);
            String cmpycd = webSocketHandler.getCmpycd(exten);
            data.put("cmpycd", cmpycd);
            Map<String, Object> params = new HashMap<>();
            params.put("cmpycd", cmpycd);
            params.put("phone", callerId);
            Map<String, Object> customer = inboundMapper.findCustomerByPhoneMap(params);
            if (customer != null) customer.forEach((k, v) -> data.put(k.toLowerCase(), v));
            else data.put("custnm", "미등록 고객");
            webSocketHandler.sendMessage(exten, objectMapper.writeValueAsString(data));
        } catch (Exception e) {}
    }

    private void sendCtiEvent(String exten, String type, String channel, String recFile) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type); data.put("exten", exten);
            if (channel != null) data.put("channel", channel);
            if (recFile != null) data.put("recordingFile", recFile + ".wav");
            webSocketHandler.sendMessage(exten, objectMapper.writeValueAsString(data));
        } catch (Exception e) {}
    }

    private String extractNumberOnly(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("(\\d{3,4})").matcher(s);
        return m.find() ? m.group(1) : null;
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (true) {
                try {
                    if (managerConnection.getState() != org.asteriskjava.manager.ManagerConnectionState.CONNECTED) {
                        managerConnection.addEventListener(this);
                        managerConnection.login();
                    }
                } catch (Exception e) {}
                try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
            }
        }).start();
    }
    @PreDestroy public void cleanup() { try { if (managerConnection != null) managerConnection.logoff(); } catch (Exception ex) {} }
}
