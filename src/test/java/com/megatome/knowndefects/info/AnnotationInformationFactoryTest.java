package com.megatome.knowndefects.info;

import com.megatome.knowndefects.Constants;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class AnnotationInformationFactoryTest {
    @Test
    public void testNullParameter() {
        assertNull(AnnotationInformationFactory.createInformation(null));
    }

    @Test
    public void testKnownDefectParameter() {
        final AnnotationInformation information = AnnotationInformationFactory.createInformation(Constants.KNOWN_DEFECT_ANNOTATION_CLASS);
        assertNotNull(information);
        assertTrue(information instanceof KnownDefectInformation);
    }

    @Test
    public void testKnownAcceptedDefectParameter() {
        final AnnotationInformation information = AnnotationInformationFactory.createInformation(Constants.KNOWN_ACCEPTED_DEFECT_ANNOTATION_CLASS);
        assertNotNull(information);
        assertTrue(information instanceof KnownAcceptedDefectInformation);
    }

    @Test
    public void testUnknownParameter() {
        assertNull(AnnotationInformationFactory.createInformation("Not a valid type"));
    }
}
