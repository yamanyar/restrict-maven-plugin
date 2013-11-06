package com.yamanyar.mvn.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import com.yamanyar.mvn.plugin.inspectors.Inspector;
import com.yamanyar.mvn.plugin.utils.WildcardMatcher;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Main entry point for the plugin
 *
 * @author Kaan Yamanyar
 */
@Mojo(name = "restrict", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RestrictMojo
        extends AbstractMojo {


    /**
     * My Map.
     */
    @Parameter
    String[] restrictions;
    @Parameter
    boolean continueOnError;

    @Parameter(defaultValue = "${project}")
    private org.apache.maven.project.MavenProject mavenProject;

    @Parameter(defaultValue = "${project.build.directory}")
    private File buildDirectory;

    public void execute()
            throws MojoExecutionException {
        File report = new File(buildDirectory, "restrict-maven-plugin.txt");
        RestrictLogger restrictLogger = new RestrictLogger(report, getLog());
        long x = System.currentTimeMillis();
        try {

            restrictLogger.info("restrict-maven-plugin started!");



        /* Restriction from classes/packages (keys) and to classes (values)*/
            Map<WildcardMatcher, Set<WildcardMatcher>> restrictionsMap;

            if (restrictions == null || restrictions.length == 0) {
                restrictLogger.warn("RestrictMojo is set as a plugin but it is not configured properly!");
                return;
            } else {
                restrictionsMap = RestrictionConfigurationFactory.produceConfiguration(restrictions, restrictLogger);
            }


            Inspector inspector = new Inspector(restrictLogger, restrictionsMap);
            try {
                Set<Artifact> artifacts = (Set<Artifact>) mavenProject.getDependencyArtifacts();


                inspector.inspectArtifacts(artifacts);
                inspector.inspectFolder(buildDirectory);

            } catch (Exception e) {
                throw new MojoExecutionException("Can not inspect all the artifacts", e);
            }


            inspector.breakIfError(continueOnError);
        } finally {
            restrictLogger.info("restrict-maven-plugin completed in approx. " + Math.ceil((System.currentTimeMillis() - x) / 1000.0) + " seconds.");
            restrictLogger.close();
        }

    }


    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }
}
