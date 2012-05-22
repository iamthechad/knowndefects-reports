package com.megatome.knowndefects.info;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class KnownDefectInformationTest {
    @Test
    public void testMethodValues() {
        final AnnotationInformation information = new KnownDefectInformation();
        final List<String> methodNames = information.getMethodNames();
        assertNotNull(methodNames);
        assertEquals(1, methodNames.size());
        assertTrue(methodNames.contains("value"));
    }
}
