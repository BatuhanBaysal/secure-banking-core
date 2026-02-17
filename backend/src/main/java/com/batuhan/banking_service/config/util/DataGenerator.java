package com.batuhan.banking_service.config.util;

import java.math.BigInteger;
import java.util.Random;

public final class DataGenerator {

    private static final Random RANDOM = new Random();
    public static final String BANK_CODE = "00088";

    public static String generateTckn(int seed) {
        int[] digits = new int[11];
        digits[0] = 1 + (seed % 9);
        for (int i = 1; i < 9; i++) digits[i] = RANDOM.nextInt(10);

        int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int evenSum = digits[1] + digits[3] + digits[5] + digits[7];

        digits[9] = ((oddSum * 7) - evenSum) % 10;
        if (digits[9] < 0) digits[9] += 10;

        int totalSum = 0;
        for (int i = 0; i < 10; i++) totalSum += digits[i];
        digits[10] = totalSum % 10;

        StringBuilder sb = new StringBuilder();
        for (int d : digits) sb.append(d);
        return sb.toString();
    }

    public static String generateIban(int seed) {
        String accountNumber = String.format("%016d", 2000000000L + seed);
        String bban = BANK_CODE + "0" + accountNumber;
        String forCheck = bban + "292700";
        int checkDigit = 98 - new BigInteger(forCheck).mod(BigInteger.valueOf(97)).intValue();
        return "TR" + String.format("%02d", checkDigit) + bban;
    }
}