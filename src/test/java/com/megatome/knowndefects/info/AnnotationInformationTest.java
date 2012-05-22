package com.megatome.knowndefects.info;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class AnnotationInformationTest {
    @Test
    public void testSort() {
        final AnnotationInformation information = new KnownDefectInformation();
        final AnnotationInformation information1 = new KnownDefectInformation();

        information.setMethodName("TestMethod");
        information1.setMethodName("MethodTest");

        final List<AnnotationInformation> list = new ArrayList<AnnotationInformation>();
        list.add(information);
        list.add(information1);

        assertTrue(list.contains(information));
        assertEquals(0, list.indexOf(information));

        Collections.sort(list);
        assertTrue(list.contains(information));
        assertEquals(1, list.indexOf(information));
    }
}
