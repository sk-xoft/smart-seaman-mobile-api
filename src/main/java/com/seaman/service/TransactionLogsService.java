package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.SessionEntity;
import com.seaman.event.TransactionLogInsertEvent;
import com.seaman.event.TransactionLogUpdateEvent;
import com.seaman.utils.RedactionUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class TransactionLogsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationEventPublisher eventPublisher;

    private final HttpServletRequest httpServletRequest;

    public void insert(String transId, String bodyReqJson, String serviceName, String createBy) {
        try {
            // อ่าน request context ใน thread ปัจจุบันก่อน publish (listener รันใน thread อื่น)
            String language = httpServletRequest.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE);
            String deviceModel = httpServletRequest.getHeader(AppSys.HEADER_DEVICE_MODEL);
            String deviceInfo = httpServletRequest.getHeader(AppSys.HEADER_DEVICE_INFO);
            String correlationId = httpServletRequest.getHeader(AppSys.HEADER_CORRELATION_ID);
            String authorization = httpServletRequest.getHeader(AppSys.HEADER_AUTHORIZATION);
            String token = authorization != null ? "(protected)" : "";

            SessionEntity sessionEntity = (SessionEntity) httpServletRequest.getAttribute("sessionObject");
            String clientSessionId = null;
            if (sessionEntity != null) {
                clientSessionId = sessionEntity.getClientSessionId();
            } else {
                clientSessionId = (String) httpServletRequest.getAttribute("clientSessionId");
            }

            eventPublisher.publishEvent(new TransactionLogInsertEvent(
                    this, transId, serviceName, RedactionUtils.redactJsonString(bodyReqJson), createBy,
                    language, deviceModel, deviceInfo, correlationId, token, clientSessionId
            ));
        } catch (Exception ex) {
            log.error("TransactionLog insert publish error -> {}", ex.getMessage());
        }
    }

    public void update(String transId, String resJson, String statusCode, String updateBy) {
        try {
            eventPublisher.publishEvent(new TransactionLogUpdateEvent(
                    this, transId, RedactionUtils.redactJsonString(resJson), statusCode, null, updateBy
            ));
        } catch (Exception ex) {
            log.error("TransactionLog update publish error -> {}", ex.getMessage());
        }
    }

    public void updateStatusMessage(String transId, String resJson, String statusCode, String statusMessage) {
        try {
            eventPublisher.publishEvent(new TransactionLogUpdateEvent(
                    this, transId, RedactionUtils.redactJsonString(resJson), statusCode, statusMessage, null
            ));
        } catch (Exception ex) {
            log.error("TransactionLog updateStatusMessage publish error -> {}", ex.getMessage());
        }
    }
}
