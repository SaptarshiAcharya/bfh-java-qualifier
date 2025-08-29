package com.example.bfh;

public class RegNoUtil {
    /**
     * Extract the last two digits from a registration number string.
     * If fewer than two digits exist, it uses what's available.
     * Returns -1 if no digits were found.
     */
    public static int lastTwoDigits(String regNo) {
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.isEmpty()) return -1;
        String tail = digits.length() >= 2 ? digits.substring(digits.length() - 2) : digits;
        return Integer.parseInt(tail);
    }

    public static boolean isOdd(int n) {
        return n % 2 != 0;
    }
}
