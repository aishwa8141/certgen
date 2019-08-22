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
        $recipientName,
        $courseName,
        $title,
        $qrCodeImage,
        $dated,
        $issuedDate,
        $signatory0Image,
        $signatory0Designation,
        $certificateName
    }
}

