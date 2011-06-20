/*
 * Copyright 2011 Tsutomu YANO.
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
package com.shelfmap.simplequery;

import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.transform;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;
import static org.apache.commons.lang.StringUtils.removeEnd;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.Transformer;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.StepFinder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
@RunWith(JUnit4.class)
public class StoriesRunner extends JUnitStories {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoriesRunner.class);
    
    @Override
    public org.jbehave.core.configuration.Configuration configuration() {
        org.jbehave.core.configuration.Configuration configuration = new MostUsefulConfiguration()
            .useStoryReporterBuilder(
                new StoryReporterBuilder()
                    .withFormats(Format.CONSOLE, Format.IDE_CONSOLE, Format.HTML)
                    .withDefaultFormats())
            .useStepFinder(new StepFinder(new StepFinder.ByLevenshteinDistance()));
        
        return configuration;
    }    
    
    public static URL codeLocationFromParentPackage(Class<?> codeLocationClass) {
        String simpleName = codeLocationClass.getSimpleName() + ".class";
        String pathOfClass = codeLocationClass.getName().replace(".", "/") + ".class";
        URL classResource = codeLocationClass.getClassLoader().getResource(pathOfClass);
        String codeLocationPath = removeEnd(classResource.getFile(), simpleName);
        return codeLocationFromPath(codeLocationPath);
    }

    public String packagePath(Class<?> codeLocationClass) {
        String classPath = this.getClass().getName().replace(".", "/");
        return removeEnd(classPath, codeLocationClass.getSimpleName());
    }

    @Override
    protected List<String> storyPaths() {
        final String classPath = packagePath(this.getClass());
        List<String> paths = new StoryFinder().findPaths(
                codeLocationFromParentPackage(this.getClass()).getFile(),
                asList("**/*.story"),
                null);

        transform(paths, new Transformer() {

            @Override
            public Object transform(Object input) {
                return classPath + input;
            }
        });

        return paths;
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        final String classPath = packagePath(this.getClass());
        List<String> paths = new StoryFinder().findPaths(
                codeLocationFromParentPackage(this.getClass()).getFile(),
                asList("**/*Steps.class"),
                null);

        transform(paths, new Transformer() {

            @Override
            public Object transform(Object input) {
                return classPath + (removeEnd((String)input, ".class"));
            }
        });

        List<Object> steps = new ArrayList<Object>();
        for (String path : paths) {
            Class<?> clazz = null;
            try {
                 clazz = Class.forName(path.replace("/", "."));
                steps.add(clazz.newInstance());
            } catch (InstantiationException ex) {
                LOGGER.error("Could not instanciate a class: " + clazz.getCanonicalName(), ex);
            } catch (IllegalAccessException ex) {
                LOGGER.error("Could not access ot the constructer of the class: " + clazz.getCanonicalName(), ex);
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Cound not load a class of path: " + path, ex);
            }
        }
        return new InstanceStepsFactory(configuration(), steps).createCandidateSteps();
    }
}
