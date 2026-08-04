package com.notifyhub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        CurrentUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authenticated user in context");
            return false;
        }

        boolean allowed = Arrays.asList(requireRole.value()).contains(currentUser.roleName());
        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Role " + currentUser.roleName() + " is not permitted for this action");
            return false;
        }

        return true;
    }
}
