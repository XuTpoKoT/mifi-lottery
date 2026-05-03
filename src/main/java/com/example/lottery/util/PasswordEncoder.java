package com.example.lottery.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordEncoder {
    private static final int BCRYPT_COST = 10;

    public static String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, rawPassword.toCharArray());
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword);
        return result.verified;
    }

    public static void main(String[] args) {
        String hash = PasswordEncoder.encode("admin123");
        System.out.println(hash);
    }
}