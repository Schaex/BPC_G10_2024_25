package com.pfaff.maximilian.r_fitter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        sdsPAGE();
        bcaAssay();
        fpAssay();
    }

    static String[][] tableFromResource(String filename,int columns) throws IOException {
        return Util.readTSVTransposed(Main.class.getResourceAsStream(filename), columns);
    }

    static void sdsPAGE() throws IOException {
        final String[][] table = tableFromResource("sds_page.txt",2);

        System.out.println("--- SDS-PAGE ---");
        System.out.println(RFit.ofLinear(table[1], table[0]));
    }

    static void bcaAssay() throws IOException {
        final String[][] table = tableFromResource("bca_assay.txt",2);

        System.out.println("--- BCA-Assay ---");
        System.out.println(RFit.ofProportional(table[0], table[1]));
    }

    static void fpAssay() throws IOException {
        String[][] table = tableFromResource("fp_assay_our_data.txt", 2);

        System.out.println("--- FP-Assay ---");
        System.out.println("with our data");
        System.out.println(RFit.ofNonLinear(table[0], table[1],
                "b + (t - b) / (1 + 10^((k - x) * h))",
                new String[]{"b", "t", "k", "h"},
                new String[]{"min(y)", "max(y)", "median(x)", "1"}));

        System.out.println();

        table = tableFromResource("fp_assay_old.txt", 9);

        System.out.println("with data from 2023");

        // Iterate over all groups from 2023
        for (int groupNr = 1, groupName = 'A'; groupNr < table.length; groupNr++, groupName++) {
            System.out.println((char) groupName);
            System.out.println(RFit.ofNonLinear(table[0], table[groupNr],
                    "b + (t - b) / (1 + 10^((k - x) * h))",
                    new String[]{"b", "t", "k", "h"},
                    new String[]{"min(y)", "max(y)", "median(x)", "1"}));
        }
    }
}
