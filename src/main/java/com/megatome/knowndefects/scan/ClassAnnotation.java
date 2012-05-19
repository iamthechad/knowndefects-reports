package com.megatome.knowndefects.scan;

import com.megatome.knowndefects.info.AnnotationInformation;

import java.util.ArrayList;
import java.util.List;

public class ClassAnnotation {
    private String packageName;
    private String className;

    private List<AnnotationInformation> annotations = new ArrayList<AnnotationInformation>();

    public ClassAnnotation(final String packageName, final String className) {
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
    }

    public List<AnnotationInformation> getAnnotations() {
        return annotations;
    }
}

