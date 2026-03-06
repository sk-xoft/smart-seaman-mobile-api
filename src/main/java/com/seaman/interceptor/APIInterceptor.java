package com.seaman.interceptor;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.exception.BusinessException;
import com.seaman.exception.MissingParameterException;
import com.seaman.utils.HttpsUtils;
import com.seaman.utils.ObjectValidatorUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;

/**
 *  this default interceptor first validate common header.
 */
@Component
@RequiredArgsConstructor
public class APIInterceptor implements HandlerInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpsUtils httpsUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Mark trace id
        String var1 = UUID.randomUUID().toString().toLowerCase();

        request.setAttribute(AppSys.TRACE_ID, var1);
        log.info("Service process start, Path name : {}, Trace -> {} ", request.getRequestURI(), var1);

        // Mark time api start
        long startTime = Instant.now().toEpochMilli();
        request.setAttribute(AppSys.API_EXECUTIME, startTime);

        // Get client ip
        request.setAttribute(AppSys.CLIENT_IP, httpsUtils.getClientIp(request));

        // Set Language is default EN
        request.setAttribute(AppSys.LANGUAGE,  request.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE) == null ? AppSys.LANG_EN : request.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE));

        // Validate Header
        String language = request.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE);
        if( null == language || !ObjectValidatorUtils.validateMandatory(language)) {
            throw new BusinessException(AppStatus.ATTRIBUTE_IS_REQUIRE, " [Header] Missing parameter Language.");
        }

        String deviceModel = request.getHeader(AppSys.HEADER_DEVICE_MODEL);
        if(null == deviceModel || !ObjectValidatorUtils.validateMandatory(deviceModel)) {
            throw new BusinessException(AppStatus.ATTRIBUTE_IS_REQUIRE, " [Header] Missing parameter device model.");
        }

        String correlationid = request.getHeader(AppSys.HEADER_CORRELATION_ID);
        if(null == correlationid || !ObjectValidatorUtils.validateMandatory(correlationid)) {
            throw new BusinessException(AppStatus.ATTRIBUTE_IS_REQUIRE, " [Header] Missing parameter correlation id.");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

        // Mark log response
        String var1 = (String) request.getAttribute(AppSys.TRACE_ID);

        // Mark time api end.
        long startTime = (Long)request.getAttribute(AppSys.API_EXECUTIME);
        long endTime = Instant.now().toEpochMilli();
        long timeElapsed = endTime - startTime;
        log.info("Service process ended, Path name : {} , Using time : {} (ms). Trace Id -> {}", request.getRequestURI(), timeElapsed, var1);
    }

}
