package net.vivans.dcim.module.location.domain.model;

import java.security.SecureRandom;

public final class LocationNodeCodeGenerator {

    public static final int CODE_LENGTH = 10;

    public static final String BASE62_ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    private LocationNodeCodeGenerator() {
    }

    public static String generate() {
        char[] chars = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            chars[i] = BASE62_ALPHABET.charAt(RANDOM.nextInt(BASE62_ALPHABET.length()));
        }
        return new String(chars);
    }
}
