/*******************************************************************************
 * Copyright (c) 2011-2013 Megatome Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.megatome.knowndefects.scan;

import com.megatome.knowndefects.info.AnnotationInformation;

import java.util.*;

/**
 * Holds the results of a annotation scan.
 */
public class AnnotationScanResults {
    private final Map<String, PackageScanResults> results = new TreeMap<String, PackageScanResults>();

    /**
     * Add a result
     * @param className Name of the class the result was found in
     * @param info Result to add
     */
    public void addResult(final String className, final AnnotationInformation info) {
        if (null == info) return;

        addToResults(className, info);
    }

    private void addToResults(final String fullClassName, final AnnotationInformation info) {
        int idx = fullClassName.lastIndexOf(".");
        final String packageName = fullClassName.substring(0, idx);
        final String className = fullClassName.substring(idx+1);
        addToResults(packageName, className, info);
    }

    private void addToResults(final String packageName, final String className, final AnnotationInformation info) {
        PackageScanResults packageScanResults = results.get(packageName);
        if (null == packageScanResults) {
            packageScanResults = new PackageScanResults(packageName);
        }
        packageScanResults.addResult(className, info);
        results.put(packageName, packageScanResults);
    }

    public void merge(final AnnotationScanResults mergeSource) {
        for (final PackageScanResults packageScanResults : mergeSource.getAllResults()) {
            if (results.containsKey(packageScanResults.getPackageName())) {
                results.get(packageScanResults.getPackageName()).merge(packageScanResults);
            } else {
                results.put(packageScanResults.getPackageName(), packageScanResults);
            }
        }
    }

    public List<PackageScanResults> getAllResults() {
        final List<PackageScanResults> allResults = new ArrayList<PackageScanResults>(results.values());
        Collections.sort(allResults);
        return allResults;
    }

    public Map<String, List<ClassAnnotation>> getKnownDefectResults() {
        final Map<String, List<ClassAnnotation>> knownDefectResults = new TreeMap<String , List<ClassAnnotation>>();
        for (final Map.Entry<String, PackageScanResults> entry : results.entrySet()) {
            if (entry.getValue().hasKnownDefectResults()) {
                knownDefectResults.put(entry.getKey(), entry.getValue().getKnownDefectResults());
            }
        }
        return knownDefectResults;
    }

    public Map<String, List<ClassAnnotation>> getKnownAcceptedDefectResults() {
        final Map<String, List<ClassAnnotation>> knownAcceptedDefectResults = new TreeMap<String , List<ClassAnnotation>>();
        for (final Map.Entry<String, PackageScanResults> entry : results.entrySet()) {
            if (entry.getValue().hasKnownAcceptedDefectResults()) {
                knownAcceptedDefectResults.put(entry.getKey(), entry.getValue().getKnownAcceptedDefectResults());
            }
        }
        return knownAcceptedDefectResults;
    }

    public boolean hasKnownDefectResults() {
        for (final Map.Entry<String, PackageScanResults> entry : results.entrySet()) {
            if (entry.getValue().hasKnownDefectResults()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasKnownAcceptedDefectResults() {
        for (final Map.Entry<String, PackageScanResults> entry : results.entrySet()) {
            if (entry.getValue().hasKnownAcceptedDefectResults()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasResults() {
        return (hasKnownDefectResults() || hasKnownAcceptedDefectResults());
    }

    public int getKnownDefectResultsCount() {
        int count = 0;
        for (final PackageScanResults packageScanResults : results.values()) {
            if (packageScanResults.hasKnownDefectResults()) {
                count += packageScanResults.getKnownDefectResultsCount();
            }
        }
        return count;
    }

    public int getKnownAcceptedDefectResultsCount() {
        int count = 0;
        for (final PackageScanResults packageScanResults : results.values()) {
            if (packageScanResults.hasKnownAcceptedDefectResults()) {
                count += packageScanResults.getKnownAcceptedDefectResultsCount();
            }
        }
        return count;
    }
}
