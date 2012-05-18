package com.megatome.knowndefects;

/*
 * Copyright 2012 Megatome Technologies LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.megatome.knowndefects.info.AnnotationInformation;
import com.megatome.knowndefects.info.KnownDefectInformation;
import com.megatome.knowndefects.scan.AnnotationScanException;
import com.megatome.knowndefects.scan.AnnotationScanResults;
import com.megatome.knowndefects.scan.AnnotationScanner;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.util.*;

/**
 * Goal for creating a report of KnownDefect annotations
 *
 * @author cjohnston
 * @goal knowndefects-report
 * @execute phase="test"
 */
public class KnownDefectsReportMojo extends AbstractMavenReport implements MavenReport {
    /**
     * <i>Maven Internal</i>: The project descriptor
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Report output dir
     *
     * @parameter expression="${project.reporting.outputDirectory}/knowndefects"
     */
    private File outputDirectory;

    /**
     * The list of directories containing source to be scanned.
     * Normally will only be test code.
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    private List sourceRoots;

    /**
     * Site renderer
     *
     * @component
     */
    private Renderer renderer;

    @Override
    protected MavenProject getProject() {
        return project;
    }

    @Override
    protected String getOutputDirectory() {
        return outputDirectory.getAbsolutePath();
    }

    @Override
    public Renderer getSiteRenderer() {
        return renderer;
    }

    @Override
    public String getOutputName() {
        return "knowndefects/index";
    }

    public List getSourceRoots() {
        return sourceRoots;
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        final List<String> things = new ArrayList<String>();
        for (Object obj : getSourceRoots()) {
            final String s = (String)obj;
            things.add(s);
        }

        final List<AnnotationScanResults> resultsList = new ArrayList<AnnotationScanResults>();
        for (final String path : things) {
            try {
                final AnnotationScanResults results = AnnotationScanner.findAnnotationsInPath(path, getLog());
                resultsList.add(results);
            } catch (AnnotationScanException e) {
                getLog().error("Could not load annotations", e);
                throw new MavenReportException("Failed to scan test classes", e);
            }
        }

        Sink sink = getSink();
        sink.head();
        sink.title();
        sink.text("Known Defects Report");
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text("Known Defects Report");
        sink.sectionTitle1_();
        sink.section1_();


        for (final AnnotationScanResults scanResults : resultsList) {
            if (scanResults.hasKnownDefectResults()) {
                for (Map.Entry<String, List<AnnotationInformation>> entry : scanResults.getKnownDefectResults().entrySet()) {
                    sink.section3();
                    sink.sectionTitle3();
                    sink.text(entry.getKey());
                    sink.sectionTitle3_();
                    sink.section3_();

                    sink.table();
                    sink.tableRow();
                    sink.tableHeaderCell();
                    sink.text("Method");
                    sink.tableHeaderCell_();
                    sink.tableHeaderCell();
                    sink.text("Line");
                    sink.tableHeaderCell_();
                    sink.tableHeaderCell();
                    sink.text("Note");
                    sink.tableHeaderCell_();
                    sink.tableRow_();

                    for (final AnnotationInformation information : entry.getValue()) {
                        final KnownDefectInformation annotationInformation = (KnownDefectInformation)information;
                        sink.tableRow();
                        sink.tableCell();
                        sink.text(annotationInformation.getMethodName());
                        sink.tableCell_();
                        sink.tableCell();
                        sink.text(String.valueOf(annotationInformation.getLineNumber()));
                        sink.tableCell_();
                        sink.tableCell();
                        sink.text(annotationInformation.getValue());
                        sink.tableCell_();
                        sink.tableRow_();
                    }

                    sink.table_();
                }
            }
        }
        /*sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text("File name");
        sink.tableHeaderCell_();
        sink.tableRow_();
        for (final AnnotationScanResults scanResults : resultsList) {
            if (scanResults.hasKnownDefectResults()) {
                for (Map.Entry<String, List<AnnotationInformation>> entry : scanResults.getKnownDefectResults().entrySet()) {
                    sink.tableRow();
                    sink.tableCell();
                    sink.text(entry.getKey());
                    sink.tableCell_();
                    sink.tableRow_();
                }
            }
        }
        sink.table_();*/
        sink.body_();
        sink.flush();
        sink.close();
    }

    @Override
    public String getDescription(Locale locale) {
        return getBundle(locale).getString("report.description");
    }

    @Override
    public String getName(Locale locale) {
        return getBundle(locale).getString("report.name");
    }

    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("knowndefects-report", locale, this.getClass().getClassLoader());
    }
}
