package com.notifyhub.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        CurrentUser currentUser = CurrentUserContext.get();
        return currentUser == null ? Optional.empty() : Optional.of(currentUser.userId());
    }
}
