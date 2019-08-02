package org.incredible.certProcessor.views;

import java.util.ArrayList;
import java.util.List;

public class HTMLVars {
    private static List<String> allVars = new ArrayList<>();
    static {
        for (SupportedVars htmlVars : SupportedVars.values()) {
            allVars.add(htmlVars.toString());
        }
    }

    public static List<String> get() {
        return allVars;
    }

    public enum SupportedVars {
        $recipient,
        $course,
        $title,
        $qrCodeImage,
        $dated,
        $dateInFormatOfWords,
        $signatoryName,
        $signatory
    }
}

