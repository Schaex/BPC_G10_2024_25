package com.pfaff.maximilian.r_fitter;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        bcaAssay();
    }

    static File fromResource(String name) {
        return new File(Objects.requireNonNull(Main.class.getResource(name)).getPath());
    }

    static void sdsPAGE() throws IOException {
        final File file = fromResource("sds_page.txt");
        final String[][] table = Util.readTSVTransposed(file, 2);

        System.out.println("--- SDS-PAGE ---");
        System.out.println(RFit.ofLinear(table[1], table[0]));
    }

    static void bcaAssay() throws IOException {
        final File file = fromResource("bca_assay.txt");
        final String[][] table = Util.readTSVTransposed(file, 2);

        System.out.println("--- BCA-Assay ---");
        System.out.println(RFit.ofProportional(table[0], table[1]));
    }
}
