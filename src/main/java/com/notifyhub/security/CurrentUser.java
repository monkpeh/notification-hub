package com.notifyhub.security;

public record CurrentUser(Long userId, String pid, String roleName) {}
