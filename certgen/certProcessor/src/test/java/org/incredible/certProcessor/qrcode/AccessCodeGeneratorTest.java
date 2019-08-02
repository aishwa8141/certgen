package org.incredible.certProcessor.qrcode;

import org.junit.Test;

import static org.junit.Assert.*;

public class AccessCodeGeneratorTest {

    @Test
    public void generate() {
        AccessCodeGenerator accessCodeGenerator = new AccessCodeGenerator(6.0);
//        System.out.println(number);
        System.out.println(accessCodeGenerator.generate());
    }
}