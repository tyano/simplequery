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
package com.shelfmap.simplequery.jbehave;

import com.shelfmap.simplequery.FileClassLoader;
import com.shelfmap.simplequery.Steps;
import com.shelfmap.simplequery.jbehave.StoryRunnerTest.MyDateConverter;
import com.shelfmap.simplequery.jbehave.StoryRunnerTest.MyReportBuilder;
import com.shelfmap.simplequery.jbehave.StoryRunnerTest.MyStoryControls;
import com.shelfmap.simplequery.jbehave.StoryRunnerTest.MyStoryLoader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import static org.jbehave.core.io.CodeLocations.*;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.*;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Tsutomu YANO
 */
@RunWith(AnnotatedEmbedderRunner.class)
@Configure(storyControls = MyStoryControls.class,
                storyLoader = MyStoryLoader.class,
                storyReporterBuilder = MyReportBuilder.class,
                parameterConverters = {MyDateConverter.class})
@UsingEmbedder(embedder = Embedder.class,
                generateViewAfterStories = true,
                ignoreFailureInStories = true,
                ignoreFailureInView = true,
                metaFilters = "-skip")
@UsingSteps(instances = {})
public class StoryRunnerTest extends InjectableEmbedder {

    @Test
    @Override
    public void run() throws Throwable {
        Embedder embedder = injectedEmbedder();
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
        
        List<Object> instances = new ArrayList<Object>();
        for (Class<?> clazz : findStepsClasses()) {
            instances.add(clazz.newInstance());
        }
        
        List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
        candidateSteps.addAll(embedder.candidateSteps());
        candidateSteps.addAll(new InstanceStepsFactory(embedder.configuration(), instances).createCandidateSteps());
        embedder.useCandidateSteps(candidateSteps);
        embedder.runStoriesAsPaths(storyPaths);
    }
    
    private List<Class<?>> findStepsClasses() throws IOException, URISyntaxException {
        FileClassLoader loader = new FileClassLoader(getClass().getClassLoader());
        URL url = getClass().getResource("");
        File current = new File(url.toURI());
        @SuppressWarnings("unchecked")
        Iterator<File> fileIterator = FileUtils.iterateFiles(current, new String[]{"class"}, true);
        List<Class<?>> stepClasses = new ArrayList<Class<?>>();
         
        while(fileIterator.hasNext()) {
            File classFile = fileIterator.next();
            Class<?> clazz = loader.loadClassFile(classFile);
            if(clazz.isAnnotationPresent(Steps.class)) {
                stepClasses.add(clazz);
            }
        }
        return stepClasses;
    }

    public static class MyStoryControls extends StoryControls {

        public MyStoryControls() {
            doDryRun(false);
            doSkipScenariosAfterFailure(false);
        }
    }

    public static class MyStoryLoader extends LoadFromClasspath {

        public MyStoryLoader() {
            super(StoryRunnerTest.class.getClassLoader());
        }
    }

    public static class MyReportBuilder extends StoryReporterBuilder {

        public MyReportBuilder() {
            this.withFormats(CONSOLE, IDE_CONSOLE, HTML).withDefaultFormats();
        }
    }

    public static class MyRegexPrefixCapturingPatternParser extends RegexPrefixCapturingPatternParser {

        public MyRegexPrefixCapturingPatternParser() {
            super("%");
        }
    }

    public static class MyDateConverter extends DateConverter {

        public MyDateConverter() {
            super(new SimpleDateFormat("yyyy-MM-dd"));
        }
    }
}
