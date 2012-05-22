package com.megatome.knowndefects.info;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class KnownAcceptedDefectInformationTest {
    @Test
    public void testMethodValues() {
        final AnnotationInformation information = new KnownAcceptedDefectInformation();
        final List<String> methodNames = information.getMethodNames();
        assertNotNull(methodNames);
        assertEquals(3, methodNames.size());
        assertTrue(methodNames.contains("author"));
        assertTrue(methodNames.contains("date"));
        assertTrue(methodNames.contains("reason"));
    }
}
