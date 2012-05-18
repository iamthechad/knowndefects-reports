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
    private final Map<String, List<AnnotationInformation>> knownDefectResults = new HashMap<String, List<AnnotationInformation>>();
    private final Map<String, List<AnnotationInformation>> knownAcceptedDefectResults = new HashMap<String, List<AnnotationInformation>>();

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

    private void addToResults(final Map<String, List<AnnotationInformation>> map, String className, AnnotationInformation info) {
        List<AnnotationInformation> l = map.get(className);
        if (null == l) {
            l = new ArrayList<AnnotationInformation>();
        }
        l.add(info);
        map.put(className, l);
    }

    /**
     * Get all found KnownDefect annotations
     * @return Map of results, with class name found in as key. May be empty.
     */
    public Map<String, List<AnnotationInformation>> getKnownDefectResults() {
        return knownDefectResults;
    }

    /**
     * Get all found KnownAndAcceptedDefect annotations
     * @return Map of results, with class name found in as key. May be empty.
     */
    public Map<String, List<AnnotationInformation>> getKnownAcceptedDefectResults() {
        return knownAcceptedDefectResults;
    }

    public boolean hasKnownDefectResults() {
        return !knownDefectResults.isEmpty();
    }

    public boolean hasKnownAcceptedDefectResults() {
        return !knownAcceptedDefectResults.isEmpty();
    }
}
