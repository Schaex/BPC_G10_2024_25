package com.pfaff.maximilian.r_fitter;

import com.github.rcaller.MessageSaver;
import com.github.rcaller.TempFileService;
import com.github.rcaller.rstuff.*;
import com.github.rcaller.util.RCodeUtils;

import java.util.HashMap;
import java.util.Map;

public class RFit {
    /**
     * Keys to access stored values inside this instance's {@link RFit#values}.
     */
    public static final String KEY_COEFFICIENTS = "coefficients";
    public static final String KEY_COEFFICIENT_NAMES = "coefficient_names";
    public static final String KEY_R_SQUARED = "r_squared";
    public static final String KEY_PREDICTED = "predicted";

    private final Map<String, String[]> values = new HashMap<>();

    protected RFit() {}

    protected RFit(ROutputParser parser) {
        for (String name : parser.getNames()) {
            values.put(name, parser.getAsStringArray(name));
        }
    }

    public String[] getValue(String key) {
        return values.get(key);
    }

    /**
     * Fit a linear model on the data.
     * @param xVals x values of the data set.
     * @param yVals y values of the data set.
     * @param functionToFit Function that will be accepted by R's lm() function.
     * @return Fitting result.
     */
    public static RFit ofLM(String[] xVals, String[] yVals, String functionToFit) {
        try (RFitter fitter = RFitter.create()) {
            final RCode code = fitter.getRCode();
            final StringBuilder builder = code.getCode();

            RCodeUtils.addArray(builder, "x", xVals, false, false);
            RCodeUtils.addArray(builder, "y", yVals, false, false);

            code.addRCode("model <- lm(y ~ " + functionToFit + ")");

            return packLM(fitter);
        }
    }

    /**
     * Function: y = m * x + t
     * @see RFit#ofLM(String[], String[], String)
     */
    public static RFit ofLinear(String[] xVals, String[] yVals) {
        return ofLM(xVals, yVals, "x");
    }

    /**
     * Function: y = m * x
     * @see RFit#ofLM(String[], String[], String)
     */
    public static RFit ofProportional(String[] xVals, String[] yVals) {
        return ofLM(xVals, yVals, "x + 0");
    }

    /**
     * Function: y = a * <html>x<sup>2</sup></html> + bx + c
     * @see RFit#ofLM(String[], String[], String)
     */
    public static RFit ofQuadratic(String[] xVals, String[] yVals) {
        return ofLM(xVals, yVals, "I(x^2) + x");
    }

    /**
     * Function: y = a * <html>x<sup>2</sup></html> + bx
     * @see RFit#ofLM(String[], String[], String)
     */
    public static RFit ofQuadraticZeroIntercept(String[] xVals, String[] yVals) {
        return ofLM(xVals, yVals, "I(x^2) + x + 0");
    }

    /**
     * Fit a non-linear model on the data.
     * @param xVals x values of the data set.
     * @param yVals y values of the data set.
     * @param functionToFit Function that will be accepted by R's lm() function.
     * @param coefficientNames Names of the fitting coefficients.
     * @param coefficientStartVals Initial values of the fitting coefficients. Choosing suitable values is crucial!
     * @return Fitting result.
     */
    public static RFit ofNonLinear(String[] xVals, String[] yVals, String functionToFit, String[] coefficientNames, String[] coefficientStartVals) {
        try (RFitter fit = RFitter.create()) {
            final RCode code = fit.getRCode();
            final StringBuilder builder = code.getCode();

            // Required because it's an external package
            code.addRCode("library(minpack.lm)");

            RCodeUtils.addArray(builder, "x", xVals, false, false);
            RCodeUtils.addArray(builder, "y", yVals, false, false);

            final String functionCall = String.join("",
                    "model <- nlsLM(y ~ ", functionToFit,
                    ", start=list(", Util.pairString(coefficientNames, coefficientStartVals), "))");

            code.addRCode(functionCall);

            return packNLS(fit);
        }
    }

    private static final String PACKING_CODE = String.join("",
            "return_vals <- list(",
            KEY_COEFFICIENTS, " = coef(summary),",
            KEY_COEFFICIENT_NAMES, " = rownames(coefficients),",
            KEY_R_SQUARED, " = r_squared,",
            KEY_PREDICTED, " = predict(model))");

    private static RFit packLM(RFitter fit) {
        final RCode code = fit.getRCode();

        // Pack all important stats in a bundle
        code.addRCode("summary <- summary(model)");
        code.addRCode("coefficients <- coef(summary)");
        code.addRCode("r_squared <- summary$r.squared");
        code.addRCode(PACKING_CODE);

        fit.runAndReturnResult("return_vals");

        return new RFit(fit.getParser());
    }

    private static RFit packNLS(RFitter fit) {
        final RCode code = fit.getRCode();

        // R-squared
        code.addRCode("rss <- sum(residuals(model)^2)");
        code.addRCode("mean <- mean(y)");
        code.addRCode("tss <- sum((y - mean)^2)");
        code.addRCode("r_squared <- 1 - (rss / tss)");

        // Pack all important stats in a bundle
        code.addRCode("summary <- summary(model)");
        code.addRCode("coefficients <- coef(summary)");
        code.addRCode(PACKING_CODE);

        fit.runAndReturnResult("return_vals");

        return new RFit(fit.getParser());
    }

    private static final String[] COEFFICIENTS = {
            "Estimate     ",
            "Std. Error   ",
            "t value      ",
            "Pr(>|t|)     "
    };

    public static String formatCoefficients(String[] coefficientNames, String[] coefficientVals) {
        final StringBuilder builder = new StringBuilder();

        // Find necessary padding
        int targetLength = 0;
        for (String name : coefficientVals) {
            final int length = name.length();

            if (length > targetLength) {
                targetLength = length;
            }
        }

        targetLength += 2;

        builder.append("Coefficient  ");

        for (String name : coefficientNames) {
            builder.append(name)
                    .append(Util.createPadding(name, targetLength));
        }

        builder.append('\n');

        int valueIndex = 0;

        for (String coefficient : COEFFICIENTS) {
            builder.append(coefficient);

            for (int j = 0; j < coefficientNames.length; j++) {
                final String val = coefficientVals[valueIndex++];

                builder.append(val)
                        .append(Util.createPadding(val, targetLength));
            }

            builder.append('\n');
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return formatCoefficients(values.get(KEY_COEFFICIENT_NAMES), values.get(KEY_COEFFICIENTS)) +
                ("R squared    " + values.get(KEY_R_SQUARED)[0]);
    }

    /**
     * Almost-copy of the {@link RCaller} class to implement the {@link AutoCloseable} interface.
     */
    private static class RFitter extends RCaller implements AutoCloseable {
        protected RFitter(RCode rCode, ROutputParser parser, RStreamHandler rOutput, RStreamHandler rError, MessageSaver messageSaver, TempFileService tempFileService, RCallerOptions rCallerOptions) {
            super(rCode, parser, rOutput, rError, messageSaver, tempFileService, rCallerOptions);
        }

        public static RFitter create() {
            final RCallerOptions rCallerOptions = RCallerOptions.create();
            return new RFitter(RCode.create(), ROutputParser.create(rCallerOptions), new RStreamHandler(null, "Output"), new RStreamHandler(null, "Error"), new MessageSaver(), new TempFileService(), rCallerOptions);
        }

        @Override
        public void close() {
            super.stopRCallerAsync();
            super.deleteTempFiles();
        }
    }
}
