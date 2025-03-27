package com.pfaff.maximilian.r_fitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class Util {
    // TODO: update method annotation!
    /**
     * Reads all lines of a file. Each line is split into, preferably, {@code columns} parts.
     * While splitting lines, the respective cells are distributed into their cells, resulting in a transposed data set.
     * @param file File to read.
     * @param columns Expected number of columns.
     * @return Transposed data set.
     */
    public static String[][] readTSVTransposed(File file, int columns) throws IOException {
        final String[] lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            lines = reader.lines().toArray(String[]::new);
        }

        final String[][] table = new String[columns][lines.length];

        for (int i = 0; i < lines.length; i++) {
            final String[] split = lines[i].split("\t");

            for (int j = 0; j < columns; j++) {
                table[j][i] = split[j];
            }
        }

        return table;
    }

    private static final char[] COMMA_DELIMITER = {',', ' '};
    private static final char[] EQUAL_DELIMITER = {' ', '=', ' '};

    /**
     * Combines two arrays into a string containing key-value pairs,
     * reminiscing the string representation of a map: <br>
     * "key1 = value1, key2 = value2, key3 = value3"
     */
    public static <A, B> String pairString(A[] array1, B[] array2) {
        final StringBuilder builder = new StringBuilder();

        final int maxI = array1.length - 1;

        for (int i = 0; i < maxI; i++) {
            builder.append(array1[i])
                    .append(EQUAL_DELIMITER, 0, EQUAL_DELIMITER.length)
                    .append(array2[i])
                    .append(COMMA_DELIMITER, 0, COMMA_DELIMITER.length);
        }

        builder.append(array1[maxI])
                .append(EQUAL_DELIMITER, 0, EQUAL_DELIMITER.length)
                .append(array2[maxI]);

        return builder.toString();
    }

    private static final String[] PADDING_PRE_CACHE;

    /**
     * @param str String that requires padding.
     * @param targetLength Sum of the input string's and the padding's lengths.
     * @return A string where the space character is repeated so that: <pre>{@code (str + padding).length() == targetLength}</pre>
     */
    public static String createPadding(String str, int targetLength) {
        final int requiredLength = targetLength - str.length();

        if (requiredLength < PADDING_PRE_CACHE.length) {
            return PADDING_PRE_CACHE[requiredLength];
        }

        return " ".repeat(requiredLength);
    }

    static {
        PADDING_PRE_CACHE = new String[32];

        for (int i = 0; i < PADDING_PRE_CACHE.length; i++) {
            PADDING_PRE_CACHE[i] = " ".repeat(i);
        }
    }

    private Util() {}
}
