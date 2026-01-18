package com.rabin.backend.util;


import java.util.regex.Pattern;

    public class ValidationUtil {

        private static final Pattern EMAIL_PATTERN =
                Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

        private static final Pattern PASSWORD_PATTERN =
                Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");

        public static boolean isValidEmail(String email) {
            return email != null && EMAIL_PATTERN.matcher(email).matches();
        }

        public static String validatePassword(String password) {
            if (password == null || password.length() < 8)
                return "Password must be at least 8 characters long";
            if (!password.matches(".*[A-Z].*"))
                return "Password must contain at least one uppercase letter";
            if (!password.matches(".*[a-z].*"))
                return "Password must contain at least one lowercase letter";
            if (!password.matches(".*\\d.*"))
                return "Password must contain at least one number";
            return null;
        }


        public static boolean isValidPhoneNumber(String phone) {
            return phone != null && phone.matches("^\\+?[0-9]{7,15}$");
        }
    }