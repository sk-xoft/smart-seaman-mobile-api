package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.utils.RedactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class LoggerService {

    private final Logger logger = LoggerFactory.getLogger(LoggerService.class);

    private static final Set<String> IMPORTANT_HEADERS = Set.of(
            "accept-language", "content-type", "correlationid", "deviceinfo", "devicemodel"
    );

    public void displayReq(HttpServletRequest request, Object body) {
        StringBuilder reqMessage = new StringBuilder();
        Map<String,String> parameters = getParameters(request);
        Map<String,String> reqHeaders = this.getHeaders(request);

        reqMessage.append("REQUEST ");
        reqMessage.append("method = [").append(request.getMethod()).append("]");
        reqMessage.append(" path = [").append(request.getRequestURI()).append("] ");

        if(!reqHeaders.isEmpty()) {
            reqMessage.append(" ReqHeaders = [").append(reqHeaders).append("]");
        }

        if(!parameters.isEmpty()) {
            reqMessage.append(" parameters = [").append(RedactionUtils.redactMap(parameters)).append("] ");
        }

        if(!Objects.isNull(body)) {
            String modifiedJsonString = RedactionUtils.redactJsonObject(body);

            reqMessage.append(" ReqBody = [").append(modifiedJsonString).append("]");

            // Set request body to attribute
            request.setAttribute(AppSys.REQUEST_BODY, modifiedJsonString);
        }

        logger.info("TraceId = {}, -> {}", request.getAttribute(AppSys.TRACE_ID), reqMessage);
    }

    public void displayRes(HttpServletRequest request, HttpServletResponse response, Object body) {
        StringBuilder respMessage = new StringBuilder();
        Map<String,String> headers = getHeaders(response);
        respMessage.append("RESPONSE ");
        respMessage.append(" method = [").append(request.getMethod()).append("]");
        if(!headers.isEmpty()) {
            respMessage.append(" ResHeaders = [").append(headers).append("]");
        }
        respMessage.append(" ResBody = [").append(RedactionUtils.redactJsonObject(body)).append("]");

        logger.info("TraceId={}, -> {}", request.getAttribute(AppSys.TRACE_ID), respMessage);
    }

    private Map<String,String> getHeaders(HttpServletResponse response) {
        Map<String,String> headers = new HashMap<>();
        Collection<String> headerMap = response.getHeaderNames();
        for(String str : headerMap) {
            headers.put(str,response.getHeader(str));
        }
        return headers;
    }

    private Map<String,String> getHeaders(HttpServletRequest request) {

        Map<String, String> map = new HashMap<>();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            if (IMPORTANT_HEADERS.contains(key.toLowerCase())) {
                map.put(key, request.getHeader(key));
            }
        }

        return map;
    }

    private Map<String,String> getParameters(HttpServletRequest request) {
        Map<String,String> parameters = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while(params.hasMoreElements()) {
            String paramName = params.nextElement();
            String paramValue = request.getParameter(paramName);
            parameters.put(paramName,paramValue);
        }

        return parameters;
    }

}
