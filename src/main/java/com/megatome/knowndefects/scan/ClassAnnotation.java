package com.megatome.knowndefects.scan;

import com.megatome.knowndefects.info.AnnotationInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassAnnotation implements Comparable<ClassAnnotation> {
    private String packageName;
    private String className;

    private List<AnnotationInformation> annotations = new ArrayList<AnnotationInformation>();

    public ClassAnnotation(final String packageName, final String className) {
        if (null == packageName || null == className) {
            throw new IllegalArgumentException("Both packageName and className must be specified");
        }
        this.packageName = packageName;
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public void addAnnotation(final AnnotationInformation information) {
        this.annotations.add(information);
        Collections.sort(annotations);
    }

    public List<AnnotationInformation> getAnnotations() {
        return annotations;
    }

    public ClassAnnotation merge(final ClassAnnotation mergeSource) {
        if (null != mergeSource) {
            annotations.addAll(mergeSource.getAnnotations());
            Collections.sort(annotations);
        }
        return this;
    }

    @Override
    public int compareTo(ClassAnnotation classAnnotation) {
        return this.className.compareTo(classAnnotation.className);
    }
}

