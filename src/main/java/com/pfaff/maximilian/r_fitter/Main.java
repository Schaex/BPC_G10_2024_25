package com.pfaff.maximilian.r_fitter;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        alleFits();
    }

    static void alleFits() throws IOException {
        System.out.println("-----Eichkurven-----");
        eichkurve();

        System.out.println();
        System.out.println("-----Dialysezeit-----");
        dialysezeit();

        System.out.println();
        System.out.println("-----GGW-Dialyse-----");
        ggwDialye();
    }

    static File fromResource(String name) {
        return new File(Objects.requireNonNull(Main.class.getResource(name)).getPath());
    }

    static void ggwDialye() throws IOException {
        final File direkt = fromResource("Direkt.txt");
        final File reziprok = fromResource("Doppelt_Reziprok.txt");
        final File scatchard = fromResource("Scatchard.txt");

        final String[][] tableDirekt = Util.readTSVTransposed(direkt, 2);
        final String[][] tableReziprok = Util.readTSVTransposed(reziprok, 2);
        final String[][] tableScatchard = Util.readTSVTransposed(scatchard, 2);

        System.out.println("Direkte Auftragung");
        System.out.println(RFit.ofNLS(tableDirekt[0], tableDirekt[1], "n * x / (k_d + x)", new String[]{"k_d", "n"}, new String[]{"100", "2"}));

        System.out.println("\nDoppelt-Reziprok");
        System.out.println(RFit.ofLinear(tableReziprok[0], tableReziprok[1]));

        System.out.println("\nScatchard-Plot");
        System.out.println(RFit.ofLinear(tableScatchard[0], tableScatchard[1]));
    }

    static void dialysezeit() throws IOException {
        File file = fromResource("Dialysezeit.txt");
        String[][] table = Util.readTSVTransposed(file, 4);

        System.out.println("Mit Ausreißern");
        System.out.println("Innen");
        System.out.println(RFit.ofNLS(table[0], table[1], "a*(1-exp(-k*x))", new String[]{"a", "k"}, new String[]{"136.35", "0.030113"}));

        System.out.println("\nAußen");
        System.out.println(RFit.ofNLS(table[0], table[2], "a*(1+exp(-k*x))", new String[]{"a", "k"}, new String[]{"136.35", "0.030113"}));

        System.out.println("\nSumme");
        System.out.println(RFit.ofLinear(table[0], table[3]));

        file = fromResource("Dialysezeit_ohne_ausreisser.txt");
        table = Util.readTSVTransposed(file, 4);

        System.out.println("\n\nOhne Ausreißer");
        System.out.println("Innen");
        System.out.println(RFit.ofNLS(table[0], table[1], "a*(1-exp(-k*x))", new String[]{"a", "k"}, new String[]{"136.35", "0.030113"}));

        System.out.println("\nAußen");
        System.out.println(RFit.ofNLS(table[0], table[2], "a*(1+exp(-k*x))", new String[]{"a", "k"}, new String[]{"136.35", "0.030113"}));

        System.out.println("\nSumme");
        System.out.println(RFit.ofLinear(table[0], table[3]));
    }

    static void eichkurve() throws IOException {
        final File file = fromResource("Eichkurve.txt");
        final String[][] table = Util.readTSVTransposed(file, 4);

        System.out.println("Serie 1, linear");
        System.out.println(RFit.ofProportional(table[0], table[1]));

        System.out.println("\nSerie 1, quadratisch");
        System.out.println(RFit.ofQuadraticZeroIntercept(table[0], table[1]));

        System.out.println("\nSerie 2, linear");
        System.out.println(RFit.ofProportional(table[0], table[2]));

        System.out.println("\nSerie 2, quadratisch");
        System.out.println(RFit.ofQuadraticZeroIntercept(table[0], table[2]));

        System.out.println("\n4 x Serie 2, linear");
        System.out.println(RFit.ofProportional(table[0], table[3]));

        System.out.println("\n4 x Serie 2, quadratisch");
        System.out.println(RFit.ofQuadraticZeroIntercept(table[0], table[3]));
    }
}
