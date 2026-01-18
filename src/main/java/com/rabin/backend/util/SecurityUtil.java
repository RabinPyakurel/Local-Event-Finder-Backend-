package com.rabin.backend.util;

import com.rabin.backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }
}
