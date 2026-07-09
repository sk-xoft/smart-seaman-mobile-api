package com.seaman.exception;

import com.google.gson.Gson;
import com.seaman.constant.AppStatus;
import com.seaman.model.response.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setCode(AppStatus.JWT_SIGNATURE_INVALID);
        exceptionResponse.setDescription("Unauthorized");
        exceptionResponse.setData(null);

        Gson gson = new Gson();
        String json = gson.toJson(exceptionResponse);

        log.warn("Unauthorized request [{} {}]", request.getMethod(), request.getRequestURI());
        response.getWriter().write(json);
    }
}
