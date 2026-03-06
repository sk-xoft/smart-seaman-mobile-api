package com.seaman.controller.advice;

import com.google.gson.Gson;
import com.seaman.constant.AppStatus;
import com.seaman.service.MessageCodeService;
import com.seaman.constant.AppSys;
import com.seaman.exception.CommonException;
import com.seaman.model.response.ExceptionResponse;
import com.seaman.service.TransactionLogsService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvice  extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final MessageCodeService messageCodeService;

    private final HttpServletRequest httpServletRequest;

    private final TransactionLogsService transactionLogsService;

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(CommonException ex) {

        // Print logs
        ex.printStackTrace();

        if(ex.getCauseException() != null){
            log.error(ExceptionUtils.getStackTrace(ex.getCauseException()));
        }

//        String lang = (String) httpServletRequest.getAttribute(AppSys.LANGUAGE);
         String lang = "TH";

        String code = ex.getCode();
        String message = messageCodeService.getMessageDescription(code, lang);

        return this.handleExceptionResponse(code, message, ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(CommonException.class)
    protected ResponseEntity<Object> handleCommonException(CommonException ex) {

        // Print logs
        ex.printStackTrace();

        if(ex.getCauseException() != null){
            log.error(ExceptionUtils.getStackTrace(ex.getCauseException()));
        }

//        String lang = (String) httpServletRequest.getAttribute(AppSys.LANGUAGE);
        String lang  =  "TH";

        String code = ex.getCode();
        String message = messageCodeService.getMessageDescription(code, lang);
        String renewMsg = "";

        if(null != ex.getMessage() || !"".equals(ex.getMessage())) {
            if (message.contains("{field_name}")) {
                renewMsg = message.replace("{field_name}", ex.getMessage());
            } else {
                renewMsg = message + " "  + ex.getMessage();
            }
        } else {
            renewMsg = message;
        }

        return this.handleExceptionResponse(code, renewMsg.trim(), ex.getStatus(), ex.getData());
    }

    private ResponseEntity<Object> handleExceptionResponse(String errorCode, String errorMessage, HttpStatus status, Object data) {
        ExceptionResponse response = new ExceptionResponse();
        response.setCode(errorCode);
        response.setDescription(errorMessage);
        response.setData(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Convert Object to JSON for print logs.
        Gson gsonConvert = new Gson();
        String jsonResponse =  gsonConvert.toJson(response, ExceptionResponse.class);
        log.error("Response exception : {}", jsonResponse);

        // Update Transaction logs
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        transactionLogsService.updateStatusMessage(transId, jsonResponse, errorCode, errorMessage);

        return ResponseEntity.status(status).headers(headers).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ex.printStackTrace();

        pageNotFoundLogger.warn(ex.getMessage());
        Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
        if (!CollectionUtils.isEmpty(supportedMethods)) {
            headers.setAllow(supportedMethods);
        }

//        String lang = (String) httpServletRequest.getAttribute(AppSys.LANGUAGE);
        String lang  =  "TH";
        String code = "MA00019";
        String message = messageCodeService.getMessageDescription(code, lang);
        return this.handleExceptionResponse(code, message, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

//        String lang = (String) httpServletRequest.getAttribute(AppSys.LANGUAGE);
        String lang  =  "TH";

        ex.printStackTrace();

        String code = AppStatus.ATTRIBUTE_IS_REQUIRE;
        String messageTemplate = messageCodeService.getMessageDescription(code, lang);
        String renewMsg = "";

        if (messageTemplate.contains("{field_name}")) {
            renewMsg = messageTemplate.replace("{field_name}", ex.getParameterName());
        }

        return this.handleExceptionResponse(code, renewMsg, status, null);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ex.printStackTrace();

//        String lang = (String) httpServletRequest.getAttribute(AppSys.LANGUAGE);
        String lang  =  "TH";
        String code = AppStatus.ATTRIBUTE_IS_REQUIRE;
        String messageTemplate = messageCodeService.getMessageDescription(code, lang);
        String renewMsg = "";

        if (messageTemplate.contains("{field_name}")) {
            renewMsg = messageTemplate.replace("{field_name}", ex.getLocalizedMessage());
        }

        return this.handleExceptionResponse(code, renewMsg, status, null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request){

//        String lang = request.getHeader(AppSys.LANGUAGE);
        String lang  =  "TH";
        String code = AppStatus.ATTRIBUTE_IS_REQUIRE;
        String messageTemplate = messageCodeService.getMessageDescription(code, lang);

        // Print stack
        ex.printStackTrace();

        /**
         * get attribute is missing parameter
         */
        // Map<String, List<String>> body = new HashMap<>();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        // body.put("errors", errors);
        log.error("Request body error : {}", errors);
        String renewMsg = "";
        if (messageTemplate.contains("{field_name}")) {
            renewMsg = messageTemplate.replace("{field_name}", errors.get(0));
        }
        return this.handleExceptionResponse(code, renewMsg, status, null);
    }
}
