package com.seaman.interceptor;

import com.seaman.constant.AppStatus;
import com.seaman.entity.SessionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.repository.UserRepository;
import com.seaman.service.JwtTokenService;
import com.seaman.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class AuthInterceptor  implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authorization = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(authorization)) {
           throw new BusinessException(AppStatus.ATTRIBUTE_IS_REQUIRE, "Authorization");
        }

        if (!authorization.startsWith("Bearer ")) {
            throw new BusinessException(AppStatus.AUTH_TYPE_HEADER, null);
        }

        String token = authorization.substring(7);
        if(jwtTokenService.validateToken(token).equals(Boolean.FALSE)){
            throw new BusinessException(AppStatus.JWT_EXPIRE, null);
        }

        String clientSessionId = jwtTokenService.getJti(token);

        if (clientSessionId == null) {
            throw new BusinessException(AppStatus.MISSING_PARAMETER, "Authorization.");
        }

        SessionEntity sessionEntity = sessionService.findById(clientSessionId);
        if(null == sessionEntity) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "session id.");
        }

//        if(sessionEntity.getToken().equals(token) && sessionEntity.getIsOnline().equals("YES")) {
        if(sessionEntity.getClientSessionId().equals(clientSessionId) && sessionEntity.getIsOnline().equals("YES")) {
            sessionService.updateStatus(sessionEntity);
        } else {
            throw new BusinessException("MA00026", "Incorrect client session id.");
        }

        if(null == sessionEntity) {
            throw new BusinessException("MA00026", "Incorrect client session id.");
        }

        // Set Session Object for request
        request.setAttribute("sessionObject", sessionEntity);

        // Set User Object for request.
        String username = jwtTokenService.getUsernameFromToken(token);
        UsersEntity usersEntity = userRepository.findByEmail(username);

        if(usersEntity == null) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "Email is not found.");
        }

        request.setAttribute("userObject", usersEntity);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        // Nothing
    }

}
