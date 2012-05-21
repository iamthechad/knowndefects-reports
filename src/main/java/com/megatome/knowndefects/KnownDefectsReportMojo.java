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
import com.megatome.knowndefects.scan.*;
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

        final AnnotationScanResults scanResults = new AnnotationScanResults();
        for (final Object obj : getSourceRoots()) {
            final String path = (String)obj;
            try {
                final AnnotationScanResults results = AnnotationScanner.findAnnotationsInPath(path, getLog());
                scanResults.merge(results);
            } catch (AnnotationScanException e) {
                getLog().error("Could not load annotations", e);
                throw new MavenReportException("Failed to scan test classes", e);
            }
        }

        final Sink sink = getSink();
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

        buildSummary(sink, scanResults);
        buildPackageList(sink, scanResults);
        buildAnnotationsList(sink, scanResults);

        sink.body_();
        sink.flush();
        sink.close();
    }

    private void buildNavLinks(final Sink sink) {
        sink.paragraph();
        sink.text("[");
        sink.link("#summary");
        sink.text("Summary");
        sink.link_();
        sink.text("][");
        sink.link("#package");
        sink.text("Package List");
        sink.link_();
        sink.text("][");
        sink.link("#annotations");
        sink.text("Annotations");
        sink.link_();
        sink.text("]");
        sink.paragraph_();
    }

    private void buildSummary(final Sink sink, final AnnotationScanResults scanResults) {
        sink.section1();
        sink.sectionTitle1();
        sink.anchor("summary");
        sink.text("Summary");
        sink.anchor_();
        sink.sectionTitle1_();

        buildNavLinks(sink);

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text("Annotation Name");
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text("Count");
        sink.tableHeaderCell_();
        sink.tableRow_();

        if (scanResults.hasKnownDefectResults()) {
            buildPackageSummaryRow(sink, "#kdpackage", "@KnownDefect", scanResults.getKnownDefectResultsCount());
        }

        if (scanResults.hasKnownAcceptedDefectResults()) {
            buildPackageSummaryRow(sink, "#kadpackage", "@KnownAndAcceptedDefect", scanResults.getKnownAcceptedDefectResultsCount());
        }

        sink.table_();
        sink.section1_();
    }

    private void buildPackageSummaryRow(final Sink sink, final String linkName, final String annotationName, final int count) {
        sink.tableRow();
        sink.tableCell();
        sink.link(linkName);
        sink.text(annotationName);
        sink.link_();
        sink.anchor_();
        sink.tableCell_();
        sink.tableCell();
        sink.text(String.valueOf(count));
        sink.tableCell_();
        sink.tableRow_();
    }

    private void buildPackageList(final Sink sink, final AnnotationScanResults scanResults) {
        sink.section1();
        sink.sectionTitle1();
        sink.anchor("package");
        sink.text("Package List");
        sink.anchor_();
        sink.sectionTitle1_();

        buildNavLinks(sink);

        if (scanResults.hasKnownDefectResults()) {
            buildPackageSummary(sink, "kdpackage", "@KnownDefect", "kd.", scanResults.getKnownDefectResults());
        }

        if (scanResults.hasKnownAcceptedDefectResults()) {
            buildPackageSummary(sink, "kadpackage", "@KnownAndAcceptedDefect", "kad.", scanResults.getKnownAcceptedDefectResults());
        }
        sink.section1_();
    }

    private void buildPackageSummary(final Sink sink, final String sectionAnchor, final String annotationName, final String anchorPrefix, final Map<String, List<ClassAnnotation>> resultMap) {
        sink.section2();
        sink.sectionTitle2();
        sink.anchor(sectionAnchor);
        sink.text(annotationName);
        sink.anchor_();
        sink.sectionTitle2_();

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text("Package");
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text("Count");
        sink.tableHeaderCell_();
        sink.tableRow_();

        for (final Map.Entry<String, List<ClassAnnotation>> entry : resultMap.entrySet()) {
            buildPackageListSummaryTable(sink, entry.getKey(), entry.getValue().size(), "#" + anchorPrefix);
        }

        sink.table_();

        for (final Map.Entry<String, List<ClassAnnotation>> entry : resultMap.entrySet()) {
            buildClassSummary(sink, entry.getKey(), entry.getValue(), anchorPrefix);
        }

        sink.section2_();
    }

    private void buildPackageListSummaryTable(final Sink sink, final String packageName, final int annotationCount, final String linkPrefix) {
        sink.tableRow();
        sink.tableCell();
        sink.link(linkPrefix + packageName);
        sink.text(packageName);
        sink.link_();
        sink.tableCell_();
        sink.tableCell();
        sink.text(String.valueOf(annotationCount));
        sink.tableCell_();
        sink.tableRow_();
    }

    private void buildClassSummary(final Sink sink, final String className, final List<ClassAnnotation> annotationList, final String anchorPrefix) {
        sink.section3();
        sink.sectionTitle3();
        sink.anchor(anchorPrefix + className);
        sink.text(className);
        sink.anchor_();
        sink.sectionTitle3_();

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text("Class");
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text("Count");
        sink.tableHeaderCell_();
        sink.tableRow_();

        for (final ClassAnnotation classAnnotation : annotationList) {
            buildClassAnnotationSummaryRow(sink, classAnnotation);
        }
        sink.table_();
        sink.section3_();
    }

    private void buildClassAnnotationSummaryRow(final Sink sink, final ClassAnnotation classAnnotation) {
        sink.tableRow();
        sink.tableCell();
        sink.link("#kd." + classAnnotation.getClassName());
        sink.text(classAnnotation.getClassName());
        sink.link_();
        sink.tableCell_();
        sink.tableCell();
        sink.text(String.valueOf(classAnnotation.getAnnotations().size()));
        sink.tableCell_();
        sink.tableRow_();
    }

    private void buildAnnotationsList(final Sink sink, final AnnotationScanResults scanResults) {
        sink.section1();
        sink.sectionTitle1();
        sink.anchor("annotations");
        sink.text("Annotations");
        sink.anchor_();
        sink.sectionTitle1_();

        buildNavLinks(sink);

        for (final PackageScanResults packageScanResults : scanResults.getAllResults()) {
            for (final String className : packageScanResults.getClassNames()) {
                sink.section2();
                sink.sectionTitle2();
                sink.anchor("kd." + className);
                sink.text(className);
                sink.sectionTitle2_();

                if (packageScanResults.hasKnownDefectResults(className)) {
                    buildMethodAnnotationSection(sink,
                            packageScanResults.getKnownDefectResults(className),
                            "@KnownDefect",
                            new String[] {"Note"},
                            new String[] {"value"});
                }

                if (packageScanResults.hasKnownAcceptedDefectResults(className)) {
                    buildMethodAnnotationSection(sink,
                            packageScanResults.getKnownAcceptedDefectResults(className),
                            "@KnownAndAcceptedDefect",
                            new String[] {"Author", "Date", "Note"},
                            new String[] {"author", "date", "reason"});
                }
                sink.section2_();
            }
        }
        sink.section1_();
    }

    private void buildMethodAnnotationSection(final Sink sink, final ClassAnnotation classAnnotation, final String sectionName, final String[] columnHeaders, final String[] columnMethodValues) {
        sink.paragraph();
        sink.bold();
        sink.text(sectionName);
        sink.bold_();
        sink.paragraph_();

        sink.table();
        buildMethodAnnotationTableHeader(sink, columnHeaders);
        buildMethodAnnotationRows(sink, classAnnotation, columnMethodValues);
        sink.table_();
    }

    private void buildMethodAnnotationTableHeader(final Sink sink, final String... columns) {
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text("Method Name");
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text("Line");
        sink.tableHeaderCell_();
        for (final String columnName : columns) {
            sink.tableHeaderCell();
            sink.text(columnName);
            sink.tableHeaderCell_();
        }
        sink.tableRow_();
    }

    private void buildMethodAnnotationRows(final Sink sink, final ClassAnnotation classAnnotation, final String... methodValues) {
        for (final AnnotationInformation information : classAnnotation.getAnnotations()) {
            sink.tableRow();
            sink.tableCell();
            sink.text(information.getMethodName());
            sink.tableCell_();
            sink.tableCell();
            sink.text(String.valueOf(information.getLineNumber()));
            sink.tableCell_();
            for (final String methodValue : methodValues) {
                sink.tableCell();
                sink.text(information.getMethodValue(methodValue));
                sink.tableCell_();
            }
            sink.tableRow_();
        }
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
