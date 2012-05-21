package com.megatome.knowndefects.scan;

import com.google.common.base.internal.Finalizer;
import com.megatome.knowndefects.info.AnnotationInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassAnnotation implements Comparable<ClassAnnotation> {
    private String packageName;
    private String className;

    private List<AnnotationInformation> annotations = new ArrayList<AnnotationInformation>();

    public ClassAnnotation(final String packageName, final String className) {
        this.packageName = packageName;
        this.className = className;
    }

    /*public ClassAnnotation(final ClassAnnotation classAnnotation) {
        this(classAnnotation.getPackageName(), classAnnotation.getClassName());
        this.annotations.addAll(classAnnotation.annotations);
        Collections.sort(annotations);
    }*/

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

    public void merge(final ClassAnnotation mergeSource) {
        annotations.addAll(mergeSource.getAnnotations());
        Collections.sort(annotations);
    }

    @Override
    public int compareTo(ClassAnnotation classAnnotation) {
        return this.className.compareTo(classAnnotation.className);
    }
}

