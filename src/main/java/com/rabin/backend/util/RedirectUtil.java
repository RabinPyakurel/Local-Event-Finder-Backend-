package com.rabin.backend.util;

import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class RedirectUtil {

    public static String buildSafeRedirectUrl(String baseUrl, Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);

        params.forEach((key, value) -> {
            if (value != null) {
                builder.queryParam(key, value);
            }
        });

        return builder.build().encode().toUriString();
    }

    public static String encodeUrlParam(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20"); // Replace + with %20 for spaces
        } catch (Exception e) {
            return param;
        }
    }
}