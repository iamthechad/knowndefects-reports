package com.megatome.knowndefects.scan;

import com.megatome.knowndefects.info.AnnotationInformation;
import com.megatome.knowndefects.info.KnownAcceptedDefectInformation;
import com.megatome.knowndefects.info.KnownDefectInformation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ClassAnnotationTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullPackageName() {
        new ClassAnnotation(null, "ClassName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullClassName() {
        new ClassAnnotation("PackageName", null);
    }

    @Test
    public void testSort() {
        final ClassAnnotation classAnnotation = new ClassAnnotation("package", "ClassName2");
        final ClassAnnotation classAnnotation1 = new ClassAnnotation("package", "ClassName1");

        final List<ClassAnnotation> list = new ArrayList<ClassAnnotation>();
        list.add(classAnnotation);
        list.add(classAnnotation1);

        Collections.sort(list);
        assertEquals(0, list.indexOf(classAnnotation1));
        assertEquals(1, list.indexOf(classAnnotation));
    }

    @Test
    public void testMerge() {
        final ClassAnnotation annotation = new ClassAnnotation("package", "Class");
        final AnnotationInformation kdInfo = new KnownDefectInformation();
        kdInfo.setMethodName("method2");
        final AnnotationInformation kdInfo1 = new KnownDefectInformation();
        kdInfo1.setMethodName("method8");
        annotation.addAnnotation(kdInfo);
        annotation.addAnnotation(kdInfo1);

        final ClassAnnotation annotation1 = new ClassAnnotation("package", "Class");
        final AnnotationInformation kadInfo = new KnownAcceptedDefectInformation();
        kadInfo.setMethodName("method1");
        final AnnotationInformation kadInfo1 = new KnownAcceptedDefectInformation();
        kadInfo1.setMethodName("method7");
        annotation1.addAnnotation(kadInfo);
        annotation1.addAnnotation(kadInfo1);

        annotation.merge(annotation1);

        final List<AnnotationInformation> merged = annotation.getAnnotations();
        assertNotNull(merged);
        assertEquals(4, merged.size());
        assertTrue(merged.contains(kadInfo));
        assertEquals(0, merged.indexOf(kadInfo));
        assertTrue(merged.contains(kdInfo));
        assertEquals(1, merged.indexOf(kdInfo));
        assertTrue(merged.contains(kadInfo1));
        assertEquals(2, merged.indexOf(kadInfo1));
        assertTrue(merged.contains(kdInfo1));
        assertEquals(3, merged.indexOf(kdInfo1));
    }
}

