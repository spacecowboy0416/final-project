package com.finalproject.coordi.user.annotation;

import com.finalproject.coordi.user.dto.UserDto;
import com.finalproject.coordi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class) &&
               UserDto.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 인증 정보가 없거나 익명 사용자인 경우 null 반환
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        // 인증 객체에 담긴 userId를 사용하여 DB 조회 후 객체 반환
        Long userId = Long.parseLong(authentication.getName());
        return userService.findById(userId);
    }
}
