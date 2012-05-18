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
/****************************************************************
 * Code in this class borrowed and adapted from the Scannotation library
 * available from http://scannotation.sourceforge.net/
 ****************************************************************/
package com.megatome.knowndefects.scan;


import com.megatome.knowndefects.info.AnnotationInformation;
import com.megatome.knowndefects.info.AnnotationInformationFactory;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.megatome.knowndefects.Constants.KNOWN_ACCEPTED_DEFECT_ANNOTATION_CLASS;
import static com.megatome.knowndefects.Constants.KNOWN_DEFECT_ANNOTATION_CLASS;

/**
 * Responsible for scanning archives to find the KD annotations.
 * <p>Code in this class borrowed and adapted from the Scannotation library
 * available from <a href="http://scannotation.sourceforge.net/">http://scannotation.sourceforge.net/</a></p>
 */
public class AnnotationScanner {
    private static final Pattern QUOTE_PATTERN = Pattern.compile("^\"(.*)\"$", Pattern.DOTALL);
    private static final AnnotationScanResults results = new AnnotationScanResults();
    private static final List<String> ignoredPackages = new ArrayList<String>(Arrays.asList("javax", "java", "sun", "com.sun", "javassist"));
    private static final Set<String> classTypes = new HashSet<String>(Arrays.asList(KNOWN_DEFECT_ANNOTATION_CLASS, KNOWN_ACCEPTED_DEFECT_ANNOTATION_CLASS));

    private AnnotationScanner() {}

    /**
     * Scan the classes found at the base path for the KD annotations. Recurses through subdirectories.
     * @param basePath Path to begin the scan at
     * @return Object containing all found annotation results
     * @throws AnnotationScanException If an error occurs
     */
    public static AnnotationScanResults findAnnotationsInPath(final String basePath, final Log log) throws AnnotationScanException {
        log.info("Looking for annotations in path " + basePath);
        if ((null == basePath) || (basePath.isEmpty())) {
            throw new IllegalArgumentException("Base path cannot be null");
        }
        try {
            scanArchives(new File(basePath), log);
        } catch (IOException e) {
            throw new AnnotationScanException("Error scanning for annotations", e);
        }

        return results;
    }

    private static void scanArchives(final File f, final Log log) throws IOException, AnnotationScanException {
        final Filter filter = new Filter() {
            public boolean accepts(String filename) {
                if (filename.endsWith(".class")) {
                    if (filename.startsWith("/")) filename = filename.substring(1);
                    if (!ignoreScan(filename.replace('/', '.'))) {
                        log.info("Filename " + filename + " accepted by filter");
                        return true;
                    }
                }

                log.info("Filename " + filename + " rejected by the filter");
                return false;
            }
        };
        final StreamIterator it = new FileIterator(f, filter);
        InputStream stream;
        while ((stream = it.next()) != null) scanClass(stream, log);
    }

    private static boolean ignoreScan(String intf) {
        for (final String ignored : ignoredPackages) {
            if (intf.startsWith(ignored + ".")) {
                return true;
            }
        }
        return false;
    }

    private static void scanClass(InputStream bits, final Log log)
            throws IOException {
        final DataInputStream dstream = new DataInputStream(new BufferedInputStream(bits));
        try {
            final ClassFile cf = new ClassFile(dstream);
            scanMethods(cf, log);
        } finally {
            dstream.close();
            bits.close();
        }
    }

    private static void scanMethods(ClassFile cf, final Log log) {
        log.info("Looking at ClassFile " + cf.getName());
        final List methods = cf.getMethods();
        if (methods == null) {
            return;
        }
        for (final Object obj : methods) {
            final MethodInfo method = (MethodInfo) obj;

            final AnnotationsAttribute visible = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.visibleTag);
            final AnnotationsAttribute invisible = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.invisibleTag);

            if (visible != null) populate(visible.getAnnotations(), method.getName(), method.getLineNumber(0), cf.getName());
            if (invisible != null) populate(invisible.getAnnotations(), method.getName(), method.getLineNumber(0), cf.getName());
        }

    }

    private static void populate(Annotation[] annotations, String methodName, int lineNumber, String className) {
        if (annotations == null) return;
        for (final Annotation ann : annotations) {
            final String annotationClass = ann.getTypeName();
            if (classTypes.contains(annotationClass)) {
                final AnnotationInformation info = AnnotationInformationFactory.createInformation(annotationClass);
                info.setClassName(className);
                info.setMethodName(methodName);
                info.setLineNumber(lineNumber);
                final Set memberNames = ann.getMemberNames();
                if (null != memberNames) {
                    for (final Object obj : memberNames) {
                        final String mName = (String)obj;
                        String value = ann.getMemberValue(mName).toString();
                        final Matcher m = QUOTE_PATTERN.matcher(value);
                        if (m.matches()) {
                            value = m.group(1);
                        }
                        info.setMethodValue(mName, value);
                    }
                }
                results.addResult(className, info);
            }
        }
    }
}
