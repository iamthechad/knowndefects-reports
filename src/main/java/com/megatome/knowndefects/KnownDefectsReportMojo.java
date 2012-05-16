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

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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

        getLog().warn("Source roots == null: " + (getSourceRoots() == null));
        getLog().warn("Source roots size: " + getSourceRoots().size());
        final List<String> things = new ArrayList<String>();
        for (Object obj : getSourceRoots()) {
            final String s = (String)obj;
            getLog().warn("Name: " + s);
            things.add(s);
        }

        Sink sink = getSink();
        sink.head();
        sink.title();
        sink.text("Some Title");
        sink.title_();
        sink.head_();

        sink.body();
        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text("File name");
        sink.tableHeaderCell_();
        sink.tableRow_();
        for (final String s : things) {
            sink.tableRow();
            sink.tableCell();
            sink.text(s);
            sink.tableCell_();
            sink.tableRow_();
        }
        sink.table_();
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
