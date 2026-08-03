package com.notifyhub.security;

import com.notifyhub.user.AppRoleEntity;
import com.notifyhub.user.AppRoleRepository;
import com.notifyhub.user.AppUserEntity;
import com.notifyhub.user.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;

    public AuthFilter(AppUserRepository appUserRepository, AppRoleRepository appRoleRepository) {
        this.appUserRepository = appUserRepository;
        this.appRoleRepository = appRoleRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String pid = request.getHeader("X-User-Pid");

        if (pid == null || pid.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-User-Pid header");
            return;
        }

        Optional<AppUserEntity> userOpt = appUserRepository.findByPid(pid);
        if (userOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown user pid: " + pid);
            return;
        }

        AppUserEntity user = userOpt.get();
        Optional<AppRoleEntity> roleOpt = appRoleRepository.findById(user.getRoleId());
        if (roleOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User has no valid role assigned");
            return;
        }

        CurrentUser currentUser = new CurrentUser(user.getId(), user.getPid(), roleOpt.get().getName());

        try {
            CurrentUserContext.set(currentUser);
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserContext.clear();
        }
    }
}
