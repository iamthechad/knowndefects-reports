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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

/**
 * Goal for creating a report of KnownDefect annotations
 *
 * @author cjohnston
 * @goal report
 * @execute phase="test"
 */
public class KnownDefectsReportMojo extends AbstractMavenReport implements MavenReport {
    public static final String KD_PREFIX = "kd.";
    public static final String KAD_PREFIX = "kad.";
    public static final String KDPACKAGE = "kdpackage";
    public static final String KADPACKAGE = "kadpackage";
    public static final String XML = "xml";
    public static final String HTML = "html";
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
     * @parameter expression="${project.reporting.outputDirectory}"
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

    /**
     * Report output format. Can be either 'xml' or 'html'. Defaults to 'html'.
     *
     * @parameter expression="${knowndefects.report.format}" default-value="html"
     */
    private String format;

    private boolean externalReport = false;

    private DocumentBuilder docBuilder = null;
    private Transformer transformer = null;


    @Override
    public boolean isExternalReport() {
        return externalReport;
    }

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
        if (XML.equalsIgnoreCase(format)) {
            getLog().info("Setting report type to XML");
            this.externalReport = true;
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            try {
                docBuilder = docFactory.newDocumentBuilder();
                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformer = transformerFactory.newTransformer();
            } catch (ParserConfigurationException e) {
                getLog().error("Could not create XML Document Builder", e);
                throw new MavenReportException("Could not create XML output", e);
            } catch (TransformerException e) {
                getLog().error("Could not create XML Document Builder", e);
                throw new MavenReportException("Could not create XML output", e);
            }
        } else if (HTML.equalsIgnoreCase(format)) {
            getLog().info("Setting report type to HTML");
            this.externalReport = false;
        } else {
            throw new MavenReportException("Specified format type " + format + " is invalid. Must be one of 'xml' or 'html'");
        }

        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                throw new MavenReportException("Could not create report output directory");
            }
        }

        final AnnotationScanResults scanResults = new AnnotationScanResults();
        for (final Object obj : getSourceRoots()) {
            final String path = (String)obj;
            try {
                scanResults.merge(AnnotationScanner.findAnnotationsInPath(path));
            } catch (AnnotationScanException e) {
                getLog().error("Could not load annotations", e);
                throw new MavenReportException("Failed to scan test classes", e);
            }
        }

        if (!isExternalReport()) {
            createHTMLReport(locale, scanResults);
        } else {
            createXMLReports(scanResults);
        }
    }

    private void createXMLReports(final AnnotationScanResults scanResults) throws MavenReportException {
        final String reportsDir = getOutputDirectory() + "/knowndefects";
        for (final PackageScanResults packageScanResults : scanResults.getAllResults()) {
            for (final String className : packageScanResults.getClassNames()) {
                final String fileName = packageScanResults.getPackageName() + "." + className + ".xml";
                final ClassAnnotation mergedAnnotations = new ClassAnnotation(packageScanResults.getPackageName(), className);
                mergedAnnotations.merge(packageScanResults.getKnownDefectResults(className)).merge(packageScanResults.getKnownAcceptedDefectResults(className));
                saveXMLReport(reportsDir, fileName, mergedAnnotations);
            }
        }
    }

    private void saveXMLReport(final String outputDirectory, final String fileName, final ClassAnnotation classResults) throws MavenReportException {
        try {
            final Document doc = docBuilder.newDocument();
            final Element rootElement = doc.createElement("annotationResults");
            doc.appendChild(rootElement);

            final Element packageElement = doc.createElement("package");
            packageElement.appendChild(doc.createTextNode(classResults.getPackageName()));
            rootElement.appendChild(packageElement);

            final Element classNameElement = doc.createElement("className");
            classNameElement.appendChild(doc.createTextNode(classResults.getClassName()));
            rootElement.appendChild(classNameElement);

            for (final AnnotationInformation information: classResults.getAnnotations()) {
                final Element annotationElement = doc.createElement("annotation");
                annotationElement.setAttribute("type", information.getAnnotationName());
                annotationElement.setAttribute("method", information.getMethodName());
                rootElement.appendChild(annotationElement);

                final Element valuesElement = doc.createElement("properties");
                annotationElement.appendChild(valuesElement);

                for (final String methodName : information.getMethodNames()) {
                    final Element valueData = doc.createElement("property");
                    valueData.setAttribute("name", methodName);
                    valueData.setAttribute("value", information.getMethodValue(methodName));
                    valuesElement.appendChild(valueData);
                }
            }

            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(new File(outputDirectory, fileName));

            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new MavenReportException("Failed to save report " + fileName, e);
        }
    }

    private void createHTMLReport(final Locale locale, final AnnotationScanResults scanResults) {
        final Sink sink = getSink();
        sink.head();
        sink.title();
        sink.text(getName(locale));
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text(getName(locale));
        sink.sectionTitle1_();
        sink.section1_();

        buildSummary(sink, locale, scanResults);
        buildPackageList(sink, locale, scanResults);
        buildAnnotationsList(sink, locale, scanResults);

        sink.body_();
        sink.flush();
        sink.close();
    }

    private void buildNavLinks(final Sink sink, final Locale locale) {
        sink.paragraph();
        sink.text("[");
        sink.link("#summary");
        sink.text(getBundle(locale).getString("summary.link.name"));
        sink.link_();
        sink.text("][");
        sink.link("#package");
        sink.text(getBundle(locale).getString("packagelist.link.name"));
        sink.link_();
        sink.text("][");
        sink.link("#annotations");
        sink.text(getBundle(locale).getString("annotations.link.name"));
        sink.link_();
        sink.text("]");
        sink.paragraph_();
    }

    private void buildSummary(final Sink sink, final Locale locale, final AnnotationScanResults scanResults) {
        sink.section1();
        sink.sectionTitle1();
        sink.anchor("summary");
        sink.text(getBundle(locale).getString("summary.link.name"));
        sink.anchor_();
        sink.sectionTitle1_();

        buildNavLinks(sink, locale);

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("annotation.name.cell.header"));
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("annotation.count.cell.header"));
        sink.tableHeaderCell_();
        sink.tableRow_();

        if (scanResults.hasKnownDefectResults()) {
            buildPackageSummaryRow(sink, KDPACKAGE, getBundle(locale).getString("knowndefect.annotation.name"), scanResults.getKnownDefectResultsCount());
        }

        if (scanResults.hasKnownAcceptedDefectResults()) {
            buildPackageSummaryRow(sink, KADPACKAGE, getBundle(locale).getString("knownaccepteddefect.annotation.name"), scanResults.getKnownAcceptedDefectResultsCount());
        }

        sink.table_();
        sink.section1_();
    }

    private void buildPackageSummaryRow(final Sink sink, final String linkName, final String annotationName, final int count) {
        sink.tableRow();
        sink.tableCell();
        sink.link("#" + linkName);
        sink.text(annotationName);
        sink.link_();
        sink.anchor_();
        sink.tableCell_();
        sink.tableCell();
        sink.text(String.valueOf(count));
        sink.tableCell_();
        sink.tableRow_();
    }

    private void buildPackageList(final Sink sink, final Locale locale, final AnnotationScanResults scanResults) {
        sink.section1();
        sink.sectionTitle1();
        sink.anchor("package");
        sink.text(getBundle(locale).getString("packagelist.link.name"));
        sink.anchor_();
        sink.sectionTitle1_();

        buildNavLinks(sink, locale);

        if (scanResults.hasKnownDefectResults()) {
            buildPackageSummary(sink, locale, KDPACKAGE, getBundle(locale).getString("knowndefect.annotation.name"), KD_PREFIX, scanResults.getKnownDefectResults());
        }

        if (scanResults.hasKnownAcceptedDefectResults()) {
            buildPackageSummary(sink, locale, KADPACKAGE, getBundle(locale).getString("knownaccepteddefect.annotation.name"), KAD_PREFIX, scanResults.getKnownAcceptedDefectResults());
        }
        sink.section1_();
    }

    private void buildPackageSummary(final Sink sink, final Locale locale, final String sectionAnchor, final String annotationName, final String anchorPrefix, final Map<String, List<ClassAnnotation>> resultMap) {
        sink.section2();
        sink.sectionTitle2();
        sink.anchor(sectionAnchor);
        sink.text(annotationName);
        sink.anchor_();
        sink.sectionTitle2_();

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("package.name.cell.header"));
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("annotation.count.cell.header"));
        sink.tableHeaderCell_();
        sink.tableRow_();

        for (final Map.Entry<String, List<ClassAnnotation>> entry : resultMap.entrySet()) {
            buildPackageListSummaryTable(sink, entry.getKey(), entry.getValue().size(), "#" + anchorPrefix);
        }

        sink.table_();

        for (final Map.Entry<String, List<ClassAnnotation>> entry : resultMap.entrySet()) {
            buildClassSummary(sink, locale, entry.getKey(), entry.getValue(), anchorPrefix);
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

    private void buildClassSummary(final Sink sink, final Locale locale, final String className, final List<ClassAnnotation> annotationList, final String anchorPrefix) {
        sink.section3();
        sink.sectionTitle3();
        sink.anchor(anchorPrefix + className);
        sink.text(className);
        sink.anchor_();
        sink.sectionTitle3_();

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("class.name.cell.header"));
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("annotation.count.cell.header"));
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
        sink.link("#" + KD_PREFIX + classAnnotation.getClassName());
        sink.text(classAnnotation.getClassName());
        sink.link_();
        sink.tableCell_();
        sink.tableCell();
        sink.text(String.valueOf(classAnnotation.getAnnotations().size()));
        sink.tableCell_();
        sink.tableRow_();
    }

    private void buildAnnotationsList(final Sink sink, final Locale locale, final AnnotationScanResults scanResults) {
        sink.section1();
        sink.sectionTitle1();
        sink.anchor("annotations");
        sink.text(getBundle(locale).getString("annotations.link.name"));
        sink.anchor_();
        sink.sectionTitle1_();

        buildNavLinks(sink, locale);

        for (final PackageScanResults packageScanResults : scanResults.getAllResults()) {
            for (final String className : packageScanResults.getClassNames()) {
                sink.section2();
                sink.sectionTitle2();
                sink.anchor(KD_PREFIX + className);
                sink.text(className);
                sink.sectionTitle2_();

                if (packageScanResults.hasKnownDefectResults(className)) {
                    buildMethodAnnotationSection(sink, locale,
                            packageScanResults.getKnownDefectResults(className),
                            getBundle(locale).getString("knowndefect.annotation.name"),
                            new String[] {getBundle(locale).getString("note.cell.header")},
                            new String[] {"value"});
                }

                if (packageScanResults.hasKnownAcceptedDefectResults(className)) {
                    buildMethodAnnotationSection(sink, locale,
                            packageScanResults.getKnownAcceptedDefectResults(className),
                            getBundle(locale).getString("knownaccepteddefect.annotation.name"),
                            new String[] {getBundle(locale).getString("author.cell.header"), getBundle(locale).getString("date.cell.header"), getBundle(locale).getString("note.cell.header")},
                            new String[] {"author", "date", "reason"});
                }
                sink.section2_();
            }
        }
        sink.section1_();
    }

    private void buildMethodAnnotationSection(final Sink sink, final Locale locale, final ClassAnnotation classAnnotation, final String sectionName, final String[] columnHeaders, final String[] columnMethodValues) {
        sink.paragraph();
        sink.bold();
        sink.text(sectionName);
        sink.bold_();
        sink.paragraph_();

        sink.table();
        buildMethodAnnotationTableHeader(sink, locale, columnHeaders);
        buildMethodAnnotationRows(sink, classAnnotation, columnMethodValues);
        sink.table_();
    }

    private void buildMethodAnnotationTableHeader(final Sink sink, final Locale locale, final String... columns) {
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("method.name.cell.header"));
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text(getBundle(locale).getString("line.number.cell.header"));
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
