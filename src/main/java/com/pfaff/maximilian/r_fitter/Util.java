package com.pfaff.maximilian.r_fitter;

import java.io.*;

public final class Util {
    /**
     * Reads all lines of a file. Each line is split into, preferably, {@code columns} parts.
     * While splitting lines, the respective cells are distributed into their cells, resulting in a transposed data set.
     * @param in InputStream to read from.
     * @param columns Number of columns inside the table.
     * @return A table.
     */
    public static String[][] readTSVTransposed(InputStream in, int columns) throws IOException {
        final String[] lines;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
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

    private static final String[] PREALLOCATED_PADDING;

    /**
     * @param str String that requires padding.
     * @param targetLength Sum of the input string's and the padding's lengths.
     * @return A string where the space character is repeated so that: <pre>{@code (str + padding).length() == targetLength}</pre>
     */
    public static String createPadding(String str, int targetLength) {
        final int requiredLength = targetLength - str.length();

        if (requiredLength < PREALLOCATED_PADDING.length) {
            return PREALLOCATED_PADDING[requiredLength];
        }

        return " ".repeat(requiredLength);
    }

    static {
        PREALLOCATED_PADDING = new String[32];

        for (int i = 0; i < PREALLOCATED_PADDING.length; i++) {
            PREALLOCATED_PADDING[i] = " ".repeat(i);
        }
    }

    private Util() {}
}
