/*******************************************************************************
 * Copyright (c) 2011 Megatome Technologies
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
import com.megatome.knowndefects.info.KnownAcceptedDefectInformation;
import com.megatome.knowndefects.info.KnownDefectInformation;

import java.util.*;

/**
 * Holds the results of a annotation scan.
 */
public class AnnotationScanResults {
    private final Map<String, List<ClassAnnotation>> knownDefectResults = new TreeMap<String, List<ClassAnnotation>>();
    private final Map<String, List<ClassAnnotation>> knownAcceptedDefectResults = new TreeMap<String, List<ClassAnnotation>>();

    /**
     * Add a result
     * @param className Name of the class the result was found in
     * @param info Result to add
     */
    public void addResult(final String className, final AnnotationInformation info) {
        if (null == info) return;

        if (info instanceof KnownDefectInformation) {
            addToResults(knownDefectResults, className, info);
        } else if (info instanceof KnownAcceptedDefectInformation) {
            addToResults(knownAcceptedDefectResults, className, info);
        }
    }

    private void addToResults(final Map<String, List<ClassAnnotation>> map, final String fullClassName, final AnnotationInformation info) {
        int idx = fullClassName.lastIndexOf(".");
        final String packageName = fullClassName.substring(0, idx);
        final String className = fullClassName.substring(idx+1);
        addToResults(map, packageName, className, info);
    }

    private void addToResults(final Map<String, List<ClassAnnotation>> map, final String packageName, final String className, final AnnotationInformation info) {
        List<ClassAnnotation> l = map.get(className);
        if (null == l) {
            l = new ArrayList<ClassAnnotation>();
        }

        ClassAnnotation classAnnotation = null;
        for (final ClassAnnotation ca : l) {
            if (ca.getClassName().equals(className)) {
                classAnnotation = ca;
                break;
            }
        }
        if (null == classAnnotation) {
            classAnnotation = new ClassAnnotation(packageName, className);
        }
        classAnnotation.addAnnotation(info);
        l.add(classAnnotation);
        map.put(packageName, l);
    }

    public void merge(final AnnotationScanResults results) {
        if (results.hasKnownDefectResults()) {
            for (final Map.Entry<String, List<ClassAnnotation>> entry : results.getKnownDefectResults().entrySet()) {
                for (final ClassAnnotation classAnnotation : entry.getValue()) {
                    for (final AnnotationInformation information : classAnnotation.getAnnotations()) {
                        addToResults(knownDefectResults, classAnnotation.getPackageName(), classAnnotation.getClassName(), information);
                    }
                }
            }
        }
    }

    /**
     * Get all found KnownDefect annotations
     * @return Map of results, with class name found in as key. May be empty.
     */
    public Map<String, List<ClassAnnotation>> getKnownDefectResults() {
        return knownDefectResults;
    }

    /**
     * Get all found KnownAndAcceptedDefect annotations
     * @return Map of results, with class name found in as key. May be empty.
     */
    public Map<String, List<ClassAnnotation>> getKnownAcceptedDefectResults() {
        return knownAcceptedDefectResults;
    }

    public boolean hasKnownDefectResults() {
        return !knownDefectResults.isEmpty();
    }

    public boolean hasKnownAcceptedDefectResults() {
        return !knownAcceptedDefectResults.isEmpty();
    }

    public int getKnownDefectResultsCount() {
        return knownDefectResults.size();
    }

    public int getKnownAcceptedDefectResultsCount() {
        return knownAcceptedDefectResults.size();
    }
}
