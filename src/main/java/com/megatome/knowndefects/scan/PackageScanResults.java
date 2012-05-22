package com.megatome.knowndefects.scan;

import com.megatome.knowndefects.info.AnnotationInformation;
import com.megatome.knowndefects.info.KnownAcceptedDefectInformation;
import com.megatome.knowndefects.info.KnownDefectInformation;

import java.util.*;

public class PackageScanResults implements Comparable<PackageScanResults> {
    private String packageName;
    private Map<String, ClassAnnotation> knownDefectResults = new TreeMap<String, ClassAnnotation>();
    private Map<String, ClassAnnotation> knownAcceptedDefectResults = new TreeMap<String, ClassAnnotation>();

    public PackageScanResults(final String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addResult(final String className, final AnnotationInformation info) {
        if (info instanceof KnownDefectInformation) {
            addResult(knownDefectResults, className, info);
        } else if (info instanceof KnownAcceptedDefectInformation) {
            addResult(knownAcceptedDefectResults, className, info);
        }
    }

    private void addResult(final Map<String, ClassAnnotation> resultList, final String className, final AnnotationInformation info) {
        ClassAnnotation classAnnotation = resultList.get(className);
        if (null == classAnnotation) {
            classAnnotation = new ClassAnnotation(packageName, className);
        }

        classAnnotation.addAnnotation(info);
        resultList.put(className, classAnnotation);
    }

    public boolean hasKnownDefectResults() {
        return !knownDefectResults.isEmpty();
    }

    public boolean hasKnownDefectResults(final String className) {
        return knownDefectResults.containsKey(className);
    }

    public int getKnownDefectResultsCount() {
        return knownDefectResults.size();
    }

    public List<ClassAnnotation> getKnownDefectResults() {
        final List<ClassAnnotation> results = new ArrayList<ClassAnnotation>(knownDefectResults.values());
        Collections.sort(results);
        return results;
    }

    public ClassAnnotation getKnownDefectResults(final String className) {
        return knownDefectResults.get(className);
    }

    public boolean hasKnownAcceptedDefectResults() {
        return !knownAcceptedDefectResults.isEmpty();
    }

    public boolean hasKnownAcceptedDefectResults(final String className) {
        return knownAcceptedDefectResults.containsKey(className);
    }

    public int getKnownAcceptedDefectResultsCount() {
        return knownAcceptedDefectResults.size();
    }

    public List<ClassAnnotation> getKnownAcceptedDefectResults() {
        final List<ClassAnnotation> results = new ArrayList<ClassAnnotation>(knownAcceptedDefectResults.values());
        Collections.sort(results);
        return results;
    }

    public ClassAnnotation getKnownAcceptedDefectResults(final String className) {
        return knownAcceptedDefectResults.get(className);
    }

    public List<String> getClassNames() {
        final Set<String> nameSet = new HashSet<String>(knownDefectResults.keySet());
        nameSet.addAll(knownAcceptedDefectResults.keySet());
        final List<String> classNames = new ArrayList<String>(nameSet);
        Collections.sort(classNames);
        return classNames;
    }

    public void merge(final PackageScanResults mergeSource) {
        for (final ClassAnnotation classAnnotation : mergeSource.getKnownDefectResults()) {
            if (knownDefectResults.containsKey(classAnnotation.getClassName())) {
                knownDefectResults.get(classAnnotation.getClassName()).merge(classAnnotation);
            } else {
                knownDefectResults.put(classAnnotation.getClassName(), classAnnotation);
            }
        }
        for (final ClassAnnotation classAnnotation : mergeSource.getKnownAcceptedDefectResults()) {
            if (knownAcceptedDefectResults.containsKey(classAnnotation.getClassName())) {
                knownAcceptedDefectResults.get(classAnnotation.getClassName()).merge(classAnnotation);
            } else {
                knownAcceptedDefectResults.put(classAnnotation.getClassName(), classAnnotation);
            }
        }
    }

    @Override
    public int compareTo(final PackageScanResults packageScanResults) {
        return this.packageName.compareTo(packageScanResults.packageName);
    }
}
