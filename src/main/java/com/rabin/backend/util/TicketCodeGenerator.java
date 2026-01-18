package com.rabin.backend.util;

import java.util.UUID;

public class TicketCodeGenerator {

    public static String generate() {
        return "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
